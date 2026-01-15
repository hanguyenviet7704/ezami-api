package com.hth.udecareer.service;

import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.model.dto.RevenueCatWebhookDto;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.Impl.RevenueCatWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueCatWebhookServiceImpl implements RevenueCatWebhookService {
    private final UserPurchasedRepository userPurchasedRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;


    private String extractEmail(RevenueCatWebhookDto.Event event) {
        if (event.getSubscriberAttributes() != null && event.getSubscriberAttributes().getEmail() != null) {
            return event.getSubscriberAttributes().getEmail().getValue();
        }
        return null;
    }

    private String mapProductToCategoryCode(RevenueCatWebhookDto.Event event) {

        if (event.getEntitlementIds() != null && event.getEntitlementIds().length > 0) {
            String entitlement = event.getEntitlementIds()[0];
            if (entitlement.startsWith("ez_")) {
                return entitlement.substring(3);
            }
        }
        return null;
    }
    private LocalDateTime millisToLocalDateTime(Long millis) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(millis),
                ZoneId.systemDefault()
        );
    }
    private void handlePurchaseEvent(String email, String categoryCode, RevenueCatWebhookDto.Event event) {
        log.info("Handle purchase event: email={}, category={}", email, categoryCode);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.warn("User not found for email: {}, skipping webhook", email);
            return;
        }
        User user = optionalUser.get();

        UserPurchasedEntity entity = userPurchasedRepository
                .findAllByUserIdOrUserEmail(user.getId(), email)
                .stream()
                .filter(e -> categoryCode.equals(e.getCategoryCode()))
                .findFirst()
                .orElse(new UserPurchasedEntity());


        entity.setUserId(user.getId());
        entity.setUserEmail(email);
        entity.setCategoryCode(categoryCode);
        entity.setIsPurchased(1);


        if (event.getPurchasedAtMs() != null) {
            entity.setFromTime(millisToLocalDateTime(event.getPurchasedAtMs()));
        } else {
            entity.setFromTime(LocalDateTime.now());
        }

        if (event.getExpirationAtMs() != null) {
            entity.setToTime(millisToLocalDateTime(event.getExpirationAtMs()));
        } else {

            entity.setToTime(null);
        }

        userPurchasedRepository.save(entity);
        log.info("Saved UserPurchasedEntity: userId={}, category={}, expiresAt={}",
                user.getId(), categoryCode, entity.getToTime());

        saveOrderFromWebhook(user, categoryCode, event);
    }
    private void handleCancellationEvent(String email, String categoryCode, RevenueCatWebhookDto.Event event) {
        log.info("Handle cancellation event: email={}, category={}", email, categoryCode);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return;
        }
        User user = optionalUser.get();

        userPurchasedRepository
                .findAllByUserIdOrUserEmail(user.getId(), email)
                .stream()
                .filter(e -> categoryCode.equals(e.getCategoryCode()))
                .findFirst()
                .ifPresent(entity -> {
                    entity.setIsPurchased(0);
                    userPurchasedRepository.save(entity);
                    log.info("Updated UserPurchasedEntity as cancelled: userId={}, category={}",
                            user.getId(), categoryCode);
                });
    }

    private void handleUncancellationEvent(String email, String categoryCode, RevenueCatWebhookDto.Event event) {
        log.info("Handle uncancellation event: email={}, category={}", email, categoryCode);

        handlePurchaseEvent(email, categoryCode, event);
    }

    private Integer extractDurationDays(RevenueCatWebhookDto.Event event) {
        String productId = event.getProductId();
        if (productId != null && productId.contains("_")) {
            String[] parts = productId.split("_");
            if (parts.length >= 3) {
                try {
                    int days = Integer.parseInt(parts[parts.length - 1]);
                    log.debug("Extracted duration_days from productId: {} days", days);
                    return days;
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse duration days from product_id: {}", productId);
                }
            }
        }

        if (event.getExpirationAtMs() != null && event.getPurchasedAtMs() != null) {
            long durationDays = ChronoUnit.DAYS.between(
                    Instant.ofEpochMilli(event.getPurchasedAtMs()),
                    Instant.ofEpochMilli(event.getExpirationAtMs())
            );
            log.debug("Calculated duration_days from timestamps: {} days (purchased: {}, expiration: {})",
                    durationDays, event.getPurchasedAtMs(), event.getExpirationAtMs());
            return (int) durationDays;
        }

        log.warn("Cannot determine duration_days, using default: 30 days. productId={}, purchasedAtMs={}, expirationAtMs={}",
                productId, event.getPurchasedAtMs(), event.getExpirationAtMs());
        return 30;
    }




    private String mapStoreToPaymentMethod(String store) {
        if (store == null) {
            return "REVENUECAT";
        }
        switch (store.toUpperCase()) {
            case "PLAY_STORE":
                return "REVENUECAT_GOOGLE";
            case "APP_STORE":
                return "REVENUECAT_APPLE";
            default:
                return "REVENUECAT";
        }
    }

    private String formatPayDate(Long purchasedAtMs) {
        if (purchasedAtMs == null) {
            return null;
        }
        LocalDateTime dateTime = millisToLocalDateTime(purchasedAtMs);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }


    private void saveOrderFromWebhook(User user, String categoryCode, RevenueCatWebhookDto.Event event) {
        try {


            Integer durationDays = extractDurationDays(event);
            String planCode = event.getPresentedOfferingId();

            BigDecimal price = event.getPrice() != null
                    ? BigDecimal.valueOf(event.getPrice())
                    : BigDecimal.ZERO;
            String paymentMethod = mapStoreToPaymentMethod(event.getStore());
            String transactionNo = event.getStoreTransactionId();
            String payDate = formatPayDate(event.getPurchasedAtMs());


            if (transactionNo != null && !transactionNo.isEmpty()) {
                Optional<OrderEntity> existingOrder = orderRepository.findByTransactionNo(transactionNo);
                if (existingOrder.isPresent()) {
                    log.info("Order already exists with transaction_no: {}, skipping. orderId={}",
                            transactionNo, existingOrder.get().getId());
                    return;
                }
            }

            OrderEntity order = OrderEntity.builder()
                    .userId(user.getId())
                    .totalAmount(price)
                    .status(OrderStatus.PAID)
                    .paymentMethod(paymentMethod)
                    .transactionNo(transactionNo)
                    .bankCode("APP")
                    .payDate(payDate)
                    .build();

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .categoryCode(categoryCode)
                    .planCode(planCode)
                    .price(price)
                    .durationDays(durationDays)
                    .build();


            List<OrderItemEntity> items = new ArrayList<>();
            items.add(orderItem);
            order.setItems(items);


            OrderEntity savedOrder = orderRepository.save(order);

            log.info("Saved Order from RevenueCat webhook: orderId={}, userId={}, category={}, plan={}, durationDays={}, amount={}, transactionNo={}",
                    savedOrder.getId(), user.getId(), categoryCode, planCode, durationDays, price, transactionNo);

        } catch (Exception e) {
            log.error("Error saving order from RevenueCat webhook: userId={}, category={}",
                    user.getId(), categoryCode, e);
        }
    }


    @Override
    @Transactional()
    public void processWebhookEvent(RevenueCatWebhookDto.WebhookEvent webhookEvent) {
        final RevenueCatWebhookDto.Event event = webhookEvent.getEvent();
        log.info("Processing RevenueCat webhook event: type={}, id={}, appUserId={}",
                event.getType(), event.getId(), event.getAppUserId());
        try{
            String email = extractEmail(event);
            if (email == null || email.isEmpty()) {
                log.warn("Cannot extract email from webhook event: {}", event.getId());
                return;
            }
            String categoryCode = mapProductToCategoryCode(event);
            if (categoryCode == null) {
                log.warn("Cannot map product to category code: productId={}", event.getProductId());
                return;
            }

            switch (event.getType()) {
                case "INITIAL_PURCHASE":
                case "RENEWAL":
                    handlePurchaseEvent(email, categoryCode, event);
                    break;

                case "CANCELLATION":
                case "EXPIRATION":
                case "BILLING_ISSUE":
                    handleCancellationEvent(email, categoryCode, event);
                    break;

                case "UNCANCELLATION":
                    handleUncancellationEvent(email, categoryCode, event);
                    break;

                default:
                    log.info("Event type {} not handled, skipping", event.getType());
            }

        }
        catch (Exception ex) {
            log.error("Error processing webhook event: {}", event.getId(), ex);
            throw ex;
        }
    }


}
