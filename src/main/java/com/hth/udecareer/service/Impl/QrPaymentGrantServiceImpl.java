package com.hth.udecareer.service.impl;

import com.hth.udecareer.entities.QrTransaction;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.SubscriptionPlanRepository;
import com.hth.udecareer.repository.QuizMasterRepository;
import com.hth.udecareer.service.QrPaymentGrantService;
import com.hth.udecareer.service.QuizCategoryService;
import com.hth.udecareer.service.UserAccessService;
import com.hth.udecareer.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrPaymentGrantServiceImpl implements QrPaymentGrantService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final QuizMasterRepository quizMasterRepository;
    private final QuizCategoryService quizCategoryService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserAccessService userAccessService;
    private final CartService cartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processQrPaymentGrant(QrTransaction tx, String usedBy) {
        if (tx == null || tx.getMessage() == null || tx.getMessage().isBlank()) return;

        var user = (usedBy == null || usedBy.isBlank()) ? null : userRepository.findByEmail(usedBy).orElse(null);

        java.util.regex.Pattern digitsPattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher digitsMatcher = digitsPattern.matcher(tx.getMessage());
        while (digitsMatcher.find()) {
            String numStr = digitsMatcher.group(1);
            try {
                long orderId = Long.parseLong(numStr);
                var ordersWithItems = orderRepository.findAllWithItemsByIds(java.util.List.of(orderId));
                if (ordersWithItems != null && !ordersWithItems.isEmpty()) {
                    var order = ordersWithItems.get(0);
                    long txAmt = 0L;
                    try { txAmt = Long.parseLong(tx.getAmount()); } catch (Exception ignore) {}
                    long orderAmt = order.getTotalAmount() == null ? 0L : order.getTotalAmount().longValue();
                    if (!com.hth.udecareer.enums.OrderStatus.PAID.equals(order.getStatus()) && txAmt == orderAmt) {
                        order.setPaymentMethod("QRPAY");
                        order.setStatus(com.hth.udecareer.enums.OrderStatus.PAID);
                        orderRepository.save(order);
                        // grant access for items
                        for (var item : order.getItems()) {
                            log.info("QR->Order mapping: grant access call for order {} item cat {} duration {}", order.getId(), item.getCategoryCode(), item.getDurationDays());
                            try {
                                userAccessService.grantAccessDirectly(order.getUserId(), item.getCategoryCode(), item.getDurationDays());
                            } catch (Exception e) {
                                log.warn("Granting access failed for user {} category {}: {}", order.getUserId(), item.getCategoryCode(), e.getMessage());
                            }
                        }
                        // Xóa giỏ hàng sau khi thanh toán thành công
                        try {
                            cartService.clearCart(order.getUserId());
                            log.info("QR->Order mapping: Cleared cart for user {}", order.getUserId());
                        } catch (Exception e) {
                            log.warn("Failed to clear cart for user {}: {}", order.getUserId(), e.getMessage());
                        }
                        log.info("QR->Order mapping: Marked order #{} paid and granted access. txId={}", order.getId(), tx.getTransactionId());
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                // skip
            }
        }

        java.util.regex.Pattern contentPattern = java.util.regex.Pattern.compile("(?i)(course|lesson|quiz|topic|post)[^0-9]*(\\d+)");
        java.util.regex.Matcher contentMatcher = contentPattern.matcher(tx.getMessage());
        if (contentMatcher.find()) {
            String type = contentMatcher.group(1).toLowerCase();
            long id = Long.parseLong(contentMatcher.group(2));
            if ("quiz".equals(type)) {
                var quizDtoOpt = quizMasterRepository.findActiveQuizById(id, java.util.List.of(com.hth.udecareer.enums.PostStatus.PUBLISH, com.hth.udecareer.enums.PostStatus.PRIVATE));
                if (quizDtoOpt.isPresent()) {
                    var quizDto = quizDtoOpt.get();
                    try {
                        var categoryOpt = quizCategoryService.getCategory(quizDto);
                        if (categoryOpt.isPresent()) {
                            String categoryCode = categoryOpt.get().getCode();
                            var defaultPlan = subscriptionPlanRepository.findByCode("PLAN_30");
                            int durationDays = defaultPlan.isPresent() ? defaultPlan.get().getDurationDays() : 30;
                            if (user != null) {
                                userAccessService.grantAccessDirectly(user.getId(), categoryCode, durationDays);
                                log.info("QR->Quiz mapping: granted access for user {} to category {} for {} days", user.getEmail(), categoryCode, durationDays);
                            } else {
                                log.warn("Cannot grant access for quiz {}: no scanning user available (usedBy=null)", id);
                            }
                        }
                    } catch (Exception ex) {
                        log.warn("Failed to map quiz->category or grant access: {}", ex.getMessage());
                    }
                }
            }
        }
    }
}
