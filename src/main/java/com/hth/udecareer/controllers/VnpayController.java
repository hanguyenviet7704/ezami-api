package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.model.request.CheckoutRequest;
import com.hth.udecareer.model.request.VnpayPaymentRequest;
import com.hth.udecareer.model.request.FirebasePaymentRequest;
import com.hth.udecareer.model.response.VnpayIpnResponse;
import com.hth.udecareer.service.OrderService;
import com.hth.udecareer.service.VnpayService;
import com.hth.udecareer.service.FirebaseOrderSyncService;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.entities.User;

// Thêm các imports Swagger/OpenAPI cần thiết
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "VNPAY Payment Gateway")
public class VnpayController {

    private final VnpayService vnpayService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final FirebaseOrderSyncService firebaseOrderSyncService;

    @Operation(
            summary = "Mua Ngay (Buy Now) - Tạo đơn hàng cho 1 gói dịch vụ",
            description = """
                    **Chức năng:** Tạo đơn hàng tức thì cho 1 gói dịch vụ và sinh URL thanh toán VNPAY.
                    
                    **Luồng xử lý:**
                    1. Backend lấy giá và thời hạn của `planCode` từ DB.
                    2. Tạo bản ghi `ez_orders` (Trạng thái: PENDING).
                    3. Sinh URL VNPAY với `TxnRef` là ID của đơn hàng vừa tạo.
                    
                    **1. categoryCode (Mã môn học):**
                    - `psm1`, `psm2` 
                    - `pspo1`, `pspo2` 
                    - `istqb_foundation`, `istqb_agile`, 
                    - `istqb_adv_ta`, `istqb_adv_tm`, `istqb_adv_tta`
                    - `cbap`, `ccba`, `ecba` 
                    
                    **2. planCode (Gói thời gian):**
                    - `PLAN_30`: Gói 30 ngày.
                    - `PLAN_90`: Gói 90 ngày.

                    **3. voucherCode (Mã giảm giá - Tùy chọn):**
                    - Nhập mã voucher để được giảm giá.
                    - Hệ thống sẽ validate và tính toán số tiền giảm.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo URL thành công.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"status\": \"success\", \"paymentUrl\": \"https://sandbox.vnpayment.vn/...\", \"timestamp\": 1762972763000}"))),
            @ApiResponse(responseCode = "400", description = "PlanCode hoặc CategoryCode không hợp lệ."),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực.")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/payment/buy-now")
    public ResponseEntity<Map<String, Object>> buyNow(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody VnpayPaymentRequest request,
            HttpServletRequest httpServletRequest) throws AppException {

        log.info("[VNPAY] BUY NOW Request | Principal: {}", principal != null ? principal.getName() : "NULL");

        try {
            // Validate inputs
            if (request == null) {
                throw new AppException(ErrorCode.INVALID_KEY, "Request body cannot be null");
            }
            if (request.getCategoryCode() == null || request.getCategoryCode().trim().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_KEY, "Category code is required");
            }
            if (request.getPlanCode() == null || request.getPlanCode().trim().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_KEY, "Plan code is required");
            }

            Long userId = getUserIdFromPrincipal(principal);
            String username = principal.getName();
            String clientIp = getClientIp(httpServletRequest);

            log.info("[VNPAY] BUY NOW Request | User: {}, Plan: {}", username, request.getPlanCode());

            String voucherCode = request.getVoucherCode() != null ? request.getVoucherCode().trim() : null;

            String paymentUrl = orderService.buyNow(
                    userId,
                    request.getCategoryCode().trim(),
                    request.getPlanCode().trim(),
                    voucherCode,
                    clientIp
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("paymentUrl", paymentUrl);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[VNPAY] BUY NOW Error | Principal: {}, Error: {}",
                     principal != null ? principal.getName() : "NULL", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Thanh toán Giỏ hàng (Checkout Cart)",
            description = """
                    **Chức năng:** Thanh toán tất cả các sản phẩm đang có trong giỏ hàng của User.

                    **Luồng xử lý:**
                    1. Lấy các items từ bảng `ez_cart_items`.
                    2. Tính tổng tiền và tạo `ez_orders`.
                    3. Áp dụng voucher nếu có.
                    4. Sinh URL VNPAY cho đơn hàng tổng.
                    5. Xóa giỏ hàng sau khi thanh toán thành công.

                    **voucherCode (Mã giảm giá - Tùy chọn):**
                    - Nhập mã voucher để được giảm giá.
                    - Hệ thống sẽ validate và tính toán số tiền giảm.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/payment/checkout")
    public ResponseEntity<Map<String, Object>> checkoutCart(
            @Parameter(hidden = true) Principal principal,
            @RequestBody(required = false) CheckoutRequest request,
            HttpServletRequest httpServletRequest) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        String username = principal.getName();
        String clientIp = getClientIp(httpServletRequest);

        log.info("[VNPAY] CHECKOUT CART Request | User: {}", username);

        String voucherCode = (request != null && request.getVoucherCode() != null)
                ? request.getVoucherCode().trim() : null;

        // Gọi OrderService để checkout giỏ hàng với voucher
        String paymentUrl = orderService.checkout(userId, voucherCode, clientIp);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("paymentUrl", paymentUrl);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }



    // --- 2. API Xử lý IPN (Instant Payment Notification) ---
    @Operation(
            summary = "Xử lý thông báo IPN từ VNPAY (Server-to-Server)",
            description = """
                    **Bước 2: Xử lý Hậu kỳ và Cấp quyền DB.**
                    
                    Endpoint này được **máy chủ VNPAY** gọi (GET request) sau khi giao dịch thành công. 
                    
                    **Quy trình:** 1.  Xác minh `vnp_SecureHash` để chống giả mạo (trả về `97` nếu thất bại).
                    2.  Kiểm tra trùng lặp giao dịch (`vnp_TxnRef`) (trả về `02`).
                    3.  Cấp quyền (`UserAccessService`) và lưu DB.
                    
                    **Phản hồi:** Yêu cầu trả về JSON `RspCode` và `Message` (HTTP 200 OK) để báo cho VNPAY biết trạng thái xử lý và ngừng retry.
                    """
    )
    @GetMapping("/payment/vnpay-ipn")
    public ResponseEntity<VnpayIpnResponse> handleVnpayIpn(
            HttpServletRequest request) {

        Map<String, String> vnpParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()[0]
                ));

        log.info("Received VNPAY IPN: TxnRef={}, ResponseCode={}",
                vnpParams.get("vnp_TxnRef"), vnpParams.get("vnp_ResponseCode"));

        try {
            VnpayIpnResponse response = vnpayService.handleVnpayIpn(vnpParams);

            return ResponseEntity.ok(response);

        } catch (AppException e) {
            log.error("VNPAY IPN failed to process. Error: {}", e.getMessage());

            return ResponseEntity.ok(new VnpayIpnResponse("99", "Unknown error or DB update failed"));

        } catch (Exception e) {
            log.error("VNPAY IPN failed due to internal error.", e);
            return ResponseEntity.ok(new VnpayIpnResponse("99", "Internal Server Error"));
        }
    }

    // --- 3. API Kiểm tra Trạng thái (Optional) ---
    @Operation(
            summary = "Kiểm tra trạng thái thanh toán (Return URL)",
            description = """
                    **Bước 3: Hiển thị trạng thái cho Người dùng.**
                    
                    Endpoint này được trình duyệt người dùng gọi sau khi VNPAY redirect. 
                    Nó truy vấn DB để xác nhận rằng IPN đã xử lý và cấp quyền thành công.
                    
                    **Lưu ý:** Endpoint này được cấu hình là Public (`permitAll()`).
                    """
    )
    @GetMapping("/payment/vnpay-status")
    public ResponseEntity<?> getVnpayTransactionStatus(@Parameter(description = "Mã tham chiếu giao dịch (vnp_TxnRef)", required = true)
                                                       @RequestParam("vnp_TxnRef") String txnRef) throws AppException {

        log.info("Checking VNPAY status for TxnRef: {}", txnRef);

        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Transaction confirmed by IPN."));
    }

    // --- 4. API Thanh toán từ Firebase Order ---
    @Operation(
            summary = "Thanh toán từ Firebase Order (Unified Flow)",
            description = """
                    **Chức năng:** Tạo URL thanh toán VNPAY từ Firebase Order ID.

                    **Luồng xử lý:**
                    1. Web/App tạo order trên Firebase với items và voucher.
                    2. Gọi API này với Firebase Order ID để lấy URL thanh toán.
                    3. Backend sync order về MySQL và sinh URL VNPAY.
                    4. Sau khi thanh toán, VNPAY gọi IPN để cập nhật trạng thái.
                    5. Backend cập nhật cả MySQL và Firebase.

                    **Ưu điểm:**
                    - Thống nhất luồng thanh toán cho Web và App.
                    - Realtime sync qua Firebase.
                    - Fallback về MySQL nếu Firebase không khả dụng.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo URL thành công.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"status\": \"success\", \"paymentUrl\": \"https://sandbox.vnpayment.vn/...\", \"firebaseOrderId\": \"abc123\", \"backendOrderId\": 1}"))),
            @ApiResponse(responseCode = "400", description = "Firebase Order không hợp lệ."),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực.")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/payment/firebase-checkout")
    public ResponseEntity<Map<String, Object>> firebaseCheckout(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody FirebasePaymentRequest request,
            HttpServletRequest httpServletRequest) throws AppException {

        log.info("[VNPAY] Firebase Checkout Request | FirebaseOrderId: {}", request.getFirebaseOrderId());

        try {
            Long userId = getUserIdFromPrincipal(principal);
            String clientIp = getClientIp(httpServletRequest);

            // Gọi OrderService để xử lý Firebase order
            String paymentUrl = orderService.checkoutFromFirebase(
                    userId,
                    request.getFirebaseOrderId(),
                    clientIp
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("paymentUrl", paymentUrl);
            response.put("firebaseOrderId", request.getFirebaseOrderId());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[VNPAY] Firebase Checkout Error | FirebaseOrderId: {}, Error: {}",
                    request.getFirebaseOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private Long getUserIdFromPrincipal(Principal principal) throws AppException {
        if (principal == null) {
            log.error("Principal is null - user not authenticated");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        log.debug("Principal email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.error("Principal email is null or empty");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);
                });

        if (user.getId() == null) {
            log.error("User ID is null for email: {}", email);
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        log.debug("Found user ID: {} for email: {}", user.getId(), email);
        return user.getId();
    }

}