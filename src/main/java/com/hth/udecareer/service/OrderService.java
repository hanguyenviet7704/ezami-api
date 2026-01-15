package com.hth.udecareer.service;

import com.hth.udecareer.entities.CartItemEntity;
import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.entities.SubscriptionPlanEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.FirebaseOrderDto;
import com.hth.udecareer.model.response.OrderHistoryResponse;
import com.hth.udecareer.model.response.OrderItemResponse;
import com.hth.udecareer.repository.CartItemRepository;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final SubscriptionPlanRepository planRepository;
    private final VnpayService vnpayService;
    private final VoucherValidationService voucherValidationService;

    private FirebaseOrderSyncService firebaseOrderSyncService;

    @Autowired
    public void setFirebaseOrderSyncService(@Lazy FirebaseOrderSyncService firebaseOrderSyncService) {
        this.firebaseOrderSyncService = firebaseOrderSyncService;
    }

    @Transactional
    public String checkout(Long userId, String clientIp) {
        return checkout(userId, null, clientIp);
    }

    @Transactional
    public String checkout(Long userId, String voucherCode, String clientIp) {
        List<CartItemEntity> cartItems = cartItemRepository.findAllByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        OrderEntity order = createBaseOrder(userId);
        List<OrderItemEntity> orderItems = new ArrayList<>();
        BigDecimal originalAmount = BigDecimal.ZERO;

        for (CartItemEntity cartItem : cartItems) {
            SubscriptionPlanEntity plan = getPlanOrThrow(cartItem.getPlanCode());
            originalAmount = originalAmount.add(plan.getPrice());
            orderItems.add(createOrderItem(order, cartItem.getCategoryCode(), cartItem.getPlanCode(), plan));
        }

        BigDecimal finalAmount = originalAmount;

        // Apply voucher if provided
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            VoucherValidationService.VoucherDiscount discount =
                    voucherValidationService.validateAndCalculateDiscount(voucherCode, userId, originalAmount);

            if (discount.hasDiscount()) {
                order.setVoucherCode(discount.getVoucherCode());
                order.setOriginalAmount(originalAmount);
                order.setDiscountAmount(discount.getDiscountAmount());
                finalAmount = discount.getFinalAmount();

                log.info("Voucher {} applied to checkout. Original: {}, Discount: {}, Final: {}",
                        voucherCode, originalAmount, discount.getDiscountAmount(), finalAmount);
            }
        }

        return finalizeOrderAndGenerateUrl(order, orderItems, finalAmount, clientIp);
    }

    @Transactional
    public String buyNow(Long userId, String categoryCode, String planCode, String clientIp) {
        return buyNow(userId, categoryCode, planCode, null, clientIp);
    }

    @Transactional
    public String buyNow(Long userId, String categoryCode, String planCode, String voucherCode, String clientIp) {
        SubscriptionPlanEntity plan = getPlanOrThrow(planCode);

        OrderEntity order = createBaseOrder(userId);
        List<OrderItemEntity> orderItems = List.of(createOrderItem(order, categoryCode, planCode, plan));

        BigDecimal originalAmount = plan.getPrice();
        BigDecimal finalAmount = originalAmount;

        // Apply voucher if provided
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            VoucherValidationService.VoucherDiscount discount =
                    voucherValidationService.validateAndCalculateDiscount(voucherCode, userId, originalAmount);

            if (discount.hasDiscount()) {
                order.setVoucherCode(discount.getVoucherCode());
                order.setOriginalAmount(originalAmount);
                order.setDiscountAmount(discount.getDiscountAmount());
                finalAmount = discount.getFinalAmount();

                log.info("Voucher {} applied to order. Original: {}, Discount: {}, Final: {}",
                        voucherCode, originalAmount, discount.getDiscountAmount(), finalAmount);
            }
        }

        return finalizeOrderAndGenerateUrl(order, orderItems, finalAmount, clientIp);
    }

    /**
     * Checkout từ Firebase Order (Unified Flow cho Web/App).
     * Đọc order từ Firebase, tạo order trên Backend, và sinh URL thanh toán.
     */
    @Transactional
    public String checkoutFromFirebase(Long userId, String firebaseOrderId, String clientIp) {
        if (firebaseOrderSyncService == null || !firebaseOrderSyncService.isFirebaseConfigured()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Firebase is not configured");
        }

        // Lấy order từ Firebase
        List<FirebaseOrderDto> userOrders = firebaseOrderSyncService.getOrdersByUserId(userId);
        FirebaseOrderDto firebaseOrder = userOrders.stream()
                .filter(o -> firebaseOrderId.equals(o.getFirebaseOrderId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Firebase order not found: " + firebaseOrderId));

        // Validate order thuộc về user
        if (!userId.equals(firebaseOrder.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Order does not belong to this user");
        }

        // Kiểm tra order chưa được thanh toán
        if ("paid".equalsIgnoreCase(firebaseOrder.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY, "Order already paid");
        }

        // Tạo order trên Backend từ Firebase data
        OrderEntity order = createBaseOrder(userId);
        List<OrderItemEntity> orderItems = new ArrayList<>();
        BigDecimal originalAmount = firebaseOrder.getOriginalAmount() != null
                ? firebaseOrder.getOriginalAmount()
                : firebaseOrder.getTotalAmount();

        if (firebaseOrder.getItems() != null) {
            for (FirebaseOrderDto.FirebaseOrderItemDto item : firebaseOrder.getItems()) {
                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setCategoryCode(item.getCategoryCode());
                orderItem.setPlanCode(item.getPlanCode());
                orderItem.setPrice(item.getPrice());
                orderItem.setDurationDays(item.getDurationDays());
                orderItem.setOrder(order);
                orderItems.add(orderItem);
            }
        }

        // Apply voucher từ Firebase order
        if (firebaseOrder.getVoucherCode() != null && !firebaseOrder.getVoucherCode().isEmpty()) {
            order.setVoucherCode(firebaseOrder.getVoucherCode());
            order.setOriginalAmount(originalAmount);
            order.setDiscountAmount(firebaseOrder.getDiscountAmount());
        }

        BigDecimal finalAmount = firebaseOrder.getTotalAmount();
        order.setTotalAmount(finalAmount);
        order.setItems(orderItems);

        OrderEntity savedOrder = orderRepository.save(order);

        // Cập nhật Firebase với backend order ID
        firebaseOrderSyncService.markOrderAsSynced(firebaseOrderId, savedOrder.getId());

        log.info("Created backend order #{} from Firebase order {}", savedOrder.getId(), firebaseOrderId);

        String txnRef = String.valueOf(savedOrder.getId());
        String orderInfo = "Thanh toan don hang #" + txnRef;

        return vnpayService.createPaymentUrlForOrder(finalAmount.longValue(), txnRef, orderInfo, clientIp);
    }

    @Transactional
    public OrderEntity createCheckoutOrder(Long userId) {
        List<CartItemEntity> cartItems = cartItemRepository.findAllByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        OrderEntity order = createBaseOrder(userId);
        List<OrderItemEntity> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemEntity cartItem : cartItems) {
            SubscriptionPlanEntity plan = getPlanOrThrow(cartItem.getPlanCode());
            totalAmount = totalAmount.add(plan.getPrice());
            orderItems.add(createOrderItem(order, cartItem.getCategoryCode(), cartItem.getPlanCode(), plan));
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);
        // Cart will be cleared after successful payment in VnpayService
        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity updatePaymentMethod(Long orderId, String paymentMethod) {
        var opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) throw new AppException(ErrorCode.INVALID_KEY, "Order not found: " + orderId);
        var order = opt.get();
        order.setPaymentMethod(paymentMethod);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity createBuyNowOrder(Long userId, String categoryCode, String planCode) {
        SubscriptionPlanEntity plan = getPlanOrThrow(planCode);
        OrderEntity order = createBaseOrder(userId);
        List<OrderItemEntity> orderItems = List.of(createOrderItem(order, categoryCode, planCode, plan));
        order.setTotalAmount(plan.getPrice());
        order.setItems(orderItems);
        return orderRepository.save(order);
    }


    private SubscriptionPlanEntity getPlanOrThrow(String planCode) {
        return planRepository.findByCode(planCode)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND, "Plan not found: " + planCode));
    }

    private OrderEntity createBaseOrder(Long userId) {
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
    private OrderItemEntity createOrderItem(OrderEntity order, String categoryCode, String planCode, SubscriptionPlanEntity plan) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setCategoryCode(categoryCode);
        orderItem.setPlanCode(planCode);
        orderItem.setPrice(plan.getPrice());
        orderItem.setDurationDays(plan.getDurationDays());
        orderItem.setOrder(order);
        return orderItem;
    }

    private String finalizeOrderAndGenerateUrl(OrderEntity order, List<OrderItemEntity> items, BigDecimal totalAmount, String clientIp) {
        order.setTotalAmount(totalAmount);
        order.setItems(items);

        OrderEntity savedOrder = orderRepository.save(order);

        // Cart will be cleared after successful payment in VnpayService
        // No need to clear cart here anymore

        String txnRef = String.valueOf(savedOrder.getId());
        String orderInfo = "Thanh toan don hang #" + txnRef;

        return vnpayService.createPaymentUrlForOrder(totalAmount.longValue(), txnRef, orderInfo, clientIp);
    }


    private OrderStatus parseOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_KEY, "Invalid status: " + status + ". Valid values: PAID, PENDING, FAILED");
        }
    }


    private LocalDateTime parseDateTime(String dateStr, boolean isStartOfDay) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate date = LocalDate.parse(dateStr);
                return isStartOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
            }
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_KEY, "Invalid date format: " + dateStr + ". Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss");
        }
    }


    @Transactional(readOnly = true)
    public Page<OrderHistoryResponse> getOrderHistory(Long userId, Pageable pageable,
                                                       String status, String fromDateStr, String toDateStr) {

        OrderStatus orderStatus = parseOrderStatus(status);
        LocalDateTime fromDate = parseDateTime(fromDateStr, true);
        LocalDateTime toDate = parseDateTime(toDateStr, false);

        Page<OrderEntity> ordersPage = orderRepository.findAllByUserIdWithFilters(
                userId, orderStatus, fromDate, toDate, pageable);

        if (ordersPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> orderIds = ordersPage.getContent().stream()
                .map(OrderEntity::getId)
                .toList();

        List<OrderEntity> ordersWithItems = orderRepository.findAllWithItemsByIds(orderIds);

        java.util.Map<Long, OrderEntity> orderMap = ordersWithItems.stream()
                .collect(java.util.stream.Collectors.toMap(OrderEntity::getId, order -> order));

        return ordersPage.map(order -> {
            OrderEntity orderWithItems = orderMap.get(order.getId());

            List<OrderItemResponse> items = orderWithItems.getItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .categoryCode(item.getCategoryCode())
                            .planCode(item.getPlanCode())
                            .price(item.getPrice())
                            .durationDays(item.getDurationDays())
                            .build())
                    .toList();

        return OrderHistoryResponse.builder()
                    .id(orderWithItems.getId())
                    .totalAmount(orderWithItems.getTotalAmount())
                    .status(orderWithItems.getStatus())
                    .paymentMethod(orderWithItems.getPaymentMethod())
                    .createdAt(orderWithItems.getCreatedAt())
            .transactionNo(orderWithItems.getTransactionNo())
                    .items(items)
                    .build();
        });
    }
}