package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.FirebaseOrderDto;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.FirebaseOrderSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller để quản lý đồng bộ orders giữa Firebase và Backend.
 * Hỗ trợ cả Web và Mobile App.
 */
@Slf4j
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Firebase Orders", description = "APIs để sync orders từ Firebase về Backend")
@SecurityRequirement(name = "bearerAuth")
public class FirebaseOrderController {

    private final FirebaseOrderSyncService firebaseOrderSyncService;
    private final UserRepository userRepository;

    /**
     * Kiểm tra trạng thái Firebase configuration.
     */
    @GetMapping("/firebase-orders/status")
    @Operation(summary = "Kiểm tra trạng thái Firebase", description = "Kiểm tra xem Firebase đã được cấu hình chưa")
    public ResponseEntity<ApiResponse> getFirebaseStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", firebaseOrderSyncService.isFirebaseConfigured());
        status.put("message", firebaseOrderSyncService.isFirebaseConfigured()
                ? "Firebase is configured and ready"
                : "Firebase is not configured. Set FIREBASE_CONFIG_BASE64 environment variable.");

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * Tạo order mới trên Firebase (cho Web sử dụng).
     */
    @PostMapping("/firebase-orders")
    @Operation(summary = "Tạo Firebase Order", description = "Tạo order mới trên Firestore cho Web checkout")
    public ResponseEntity<ApiResponse> createFirebaseOrder(
            Principal principal,
            @RequestBody FirebaseOrderDto orderDto) {

        Long userId = getUserIdFromPrincipal(principal);
        String userEmail = principal.getName();

        // Set user info
        orderDto.setUserId(userId);
        orderDto.setUserEmail(userEmail);
        orderDto.setSource("WEB");

        String firebaseOrderId = firebaseOrderSyncService.createFirebaseOrder(orderDto);

        Map<String, String> result = new HashMap<>();
        result.put("firebaseOrderId", firebaseOrderId);
        result.put("message", "Order created successfully on Firebase");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Sync một order cụ thể từ Firebase về Backend.
     */
    @PostMapping("/firebase-orders/{firebaseOrderId}/sync")
    @Operation(summary = "Sync Firebase Order", description = "Đồng bộ một order từ Firebase về Backend MySQL")
    public ResponseEntity<ApiResponse> syncOrder(
            @PathVariable String firebaseOrderId) {

        Optional<OrderEntity> order = firebaseOrderSyncService.syncOrderById(firebaseOrderId);

        if (order.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(order.get()));
        } else {
            throw new AppException(ErrorCode.NOT_FOUND, "Order not found or sync failed");
        }
    }

    /**
     * Sync tất cả orders pending từ Firebase về Backend (Admin only).
     */
    @PostMapping("/firebase-orders/sync-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sync All Pending Orders", description = "Đồng bộ tất cả orders chưa sync từ Firebase (Admin)")
    public ResponseEntity<ApiResponse> syncAllPendingOrders() {
        int syncedCount = firebaseOrderSyncService.syncPendingOrders();

        Map<String, Object> result = new HashMap<>();
        result.put("syncedCount", syncedCount);
        result.put("message", String.format("Successfully synced %d orders from Firebase", syncedCount));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Lấy danh sách orders từ Firebase của user hiện tại.
     */
    @GetMapping("/firebase-orders/my-orders")
    @Operation(summary = "Get My Firebase Orders", description = "Lấy danh sách orders từ Firebase của user hiện tại")
    public ResponseEntity<ApiResponse> getMyFirebaseOrders(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        List<FirebaseOrderDto> orders = firebaseOrderSyncService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Cập nhật trạng thái order trên Firebase (cho VNPAY callback sử dụng).
     */
    @PutMapping("/firebase-orders/{firebaseOrderId}/status")
    @Operation(summary = "Update Firebase Order Status", description = "Cập nhật trạng thái order trên Firebase")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable String firebaseOrderId,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        String transactionNo = body.get("transactionNo");
        String bankCode = body.get("bankCode");
        String payDate = body.get("payDate");

        firebaseOrderSyncService.updateFirebaseOrderStatus(
                firebaseOrderId, status, transactionNo, bankCode, payDate);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Order status updated successfully");
        result.put("status", status);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Webhook để nhận thông báo từ Firebase khi có order mới (optional).
     * Có thể sử dụng Firebase Cloud Functions để gọi endpoint này.
     */
    @PostMapping("/firebase-orders/webhook")
    @Operation(summary = "Firebase Order Webhook", description = "Webhook nhận thông báo order mới từ Firebase")
    public ResponseEntity<ApiResponse> handleFirebaseWebhook(
            @RequestBody Map<String, Object> payload) {

        log.info("Received Firebase order webhook: {}", payload);

        String firebaseOrderId = (String) payload.get("firebaseOrderId");
        String eventType = (String) payload.get("eventType"); // created, updated, paid

        Map<String, Object> result = new HashMap<>();
        result.put("received", true);

        if ("paid".equals(eventType) && firebaseOrderId != null) {
            Optional<OrderEntity> order = firebaseOrderSyncService.syncOrderById(firebaseOrderId);
            if (order.isPresent()) {
                result.put("synced", true);
                result.put("backendOrderId", order.get().getId());
            } else {
                result.put("synced", false);
                result.put("error", "Failed to sync order");
            }
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        return user.getId();
    }
}
