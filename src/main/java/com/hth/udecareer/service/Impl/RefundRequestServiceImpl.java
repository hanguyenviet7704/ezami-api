package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.entities.RefundRequestEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.NotificationType;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.enums.RefundRequestStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.RefundRequestCreateRequest;
import com.hth.udecareer.model.request.RefundRequestDecisionRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.repository.RefundRequestRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.NotificationService;
import com.hth.udecareer.service.RefundRequestService;
import com.hth.udecareer.service.UserAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundRequestServiceImpl implements RefundRequestService {

    private static final int REFUND_ELIGIBLE_DAYS = 7; // Cho phép refund trong 7 ngày

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RefundRequestEntity createMyRequest(Principal principal, RefundRequestCreateRequest request) {
        Long userId = getUserIdFromPrincipal(principal);

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (!userId.equals(order.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new AppException(ErrorCode.REFUND_ORDER_NOT_ELIGIBLE, "Order is not in PAID status");
        }
        if (refundRequestRepository.existsByOrderIdAndStatus(order.getId(), RefundRequestStatus.PENDING)) {
            throw new AppException(ErrorCode.REFUND_ALREADY_PROCESSED, "A pending refund request already exists for this order");
        }

        // Check if order is within refund eligibility period
        if (order.getCreatedAt() != null) {
            long daysSincePurchase = ChronoUnit.DAYS.between(order.getCreatedAt(), LocalDateTime.now());
            if (daysSincePurchase > REFUND_ELIGIBLE_DAYS) {
                throw new AppException(ErrorCode.REFUND_ORDER_NOT_ELIGIBLE,
                        String.format("Refund is only available within %d days of purchase", REFUND_ELIGIBLE_DAYS));
            }
        }

        RefundRequestEntity entity = RefundRequestEntity.builder()
                .orderId(order.getId())
                .userId(userId)
                .status(RefundRequestStatus.PENDING)
                .reason(request.getReason())
                .description(request.getDescription())
                .build();
        return refundRequestRepository.save(entity);
    }

    @Override
    public PageResponse<RefundRequestEntity> getMyRequests(Principal principal, int page, int size) {
        Long userId = getUserIdFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RefundRequestEntity> p = refundRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.of(p);
    }

    @Override
    public PageResponse<RefundRequestEntity> getAllRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RefundRequestEntity> p = refundRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(p);
    }

    @Override
    @Transactional
    public RefundRequestEntity approve(Long requestId, RefundRequestDecisionRequest body) {
        RefundRequestEntity entity = refundRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND));

        if (entity.getStatus() != RefundRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REFUND_ALREADY_PROCESSED, "This refund request has already been processed");
        }

        // Get the order to revoke access
        OrderEntity order = orderRepository.findById(entity.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));

        // Fetch order items
        List<OrderItemEntity> items = order.getItems();
        if (items == null || items.isEmpty()) {
            order = orderRepository.findWithItemsById(entity.getOrderId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));
            items = order.getItems();
        }

        // Revoke access for each item in the order
        for (OrderItemEntity item : items) {
            userAccessService.revokeAccess(order.getUserId(), item.getCategoryCode());
            log.info("Revoked access for category {} due to refund approval", item.getCategoryCode());
        }

        // Update order status to REFUNDED
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        // Update refund request status
        entity.setStatus(RefundRequestStatus.APPROVED);
        entity.setRejectNote(null);
        RefundRequestEntity savedEntity = refundRequestRepository.save(entity);

        // Send notification to user
        notificationService.createNotification(
                entity.getUserId(),
                "Yêu cầu hoàn tiền được chấp nhận",
                String.format("Yêu cầu hoàn tiền cho đơn hàng #%d đã được chấp nhận. Số tiền %,d VNĐ sẽ được hoàn lại.",
                        order.getId(), order.getTotalAmount().longValue()),
                NotificationType.ORDER_REFUNDED,
                "/refund-requests/" + requestId
        );

        log.info("Refund request #{} approved for order #{}", requestId, order.getId());
        return savedEntity;
    }

    @Override
    @Transactional
    public RefundRequestEntity reject(Long requestId, RefundRequestDecisionRequest body) {
        RefundRequestEntity entity = refundRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND));

        if (entity.getStatus() != RefundRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REFUND_ALREADY_PROCESSED, "This refund request has already been processed");
        }

        entity.setStatus(RefundRequestStatus.REJECTED);
        entity.setRejectNote(body != null ? body.getNote() : null);
        RefundRequestEntity savedEntity = refundRequestRepository.save(entity);

        // Send notification to user
        String message = "Yêu cầu hoàn tiền cho đơn hàng #" + entity.getOrderId() + " đã bị từ chối.";
        if (body != null && body.getNote() != null && !body.getNote().isEmpty()) {
            message += " Lý do: " + body.getNote();
        }
        notificationService.createNotification(
                entity.getUserId(),
                "Yêu cầu hoàn tiền bị từ chối",
                message,
                NotificationType.SYSTEM_INFO,
                "/refund-requests/" + requestId
        );

        log.info("Refund request #{} rejected for order #{}", requestId, entity.getOrderId());
        return savedEntity;
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        return user.getId();
    }
}
