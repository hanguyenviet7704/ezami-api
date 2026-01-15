package com.hth.udecareer.service;

import com.hth.udecareer.config.VnpayConfigProperties;
import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.enums.NotificationType;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.utils.VnpayHashUtil;
import com.hth.udecareer.model.response.VnpayIpnResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnpayService {

    private final VnpayConfigProperties vnpayConfig;
    private final VnpayHashUtil vnpayHashUtil;
    private final UserAccessService userAccessService;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final CartService cartService;
    private final VoucherValidationService voucherValidationService;

    private FirebaseOrderSyncService firebaseOrderSyncService;

    @Autowired
    public void setFirebaseOrderSyncService(@Lazy FirebaseOrderSyncService firebaseOrderSyncService) {
        this.firebaseOrderSyncService = firebaseOrderSyncService;
    }

    public String createPaymentUrlForOrder(long totalAmount, String txnRef, String orderInfo, String clientIp) {

        // 1. Lấy cấu hình VNPAY
        String tmnCode = vnpayConfig.getTmnCode();
        String apiUrl = vnpayConfig.getApiUrl();
        String hashSecret = vnpayConfig.getHashSecret();
        String returnUrl = vnpayConfig.getReturnUrl();
        String version = vnpayConfig.getVersion() != null ? vnpayConfig.getVersion() : "2.1.0";
        String locale = vnpayConfig.getLocale() != null ? vnpayConfig.getLocale() : "vn";

        try {
            log.info("[VNPAY] Create Order URL | TxnRef: {}, Amount: {}", txnRef, totalAmount);

            // Encode order info with proper URL encoding
            String vnp_OrderInfo = URLEncoder.encode(orderInfo, StandardCharsets.UTF_8).replace("+", "%20");
            log.debug("Encoded OrderInfo: {}", vnp_OrderInfo);

            // Ép cứng TimeZone là Việt Nam (GMT+7)
            TimeZone vnTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(vnTimeZone);

            String createDate = sdf.format(new Date());

            Calendar expire = Calendar.getInstance(vnTimeZone);
            expire.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = sdf.format(expire.getTime());

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", version);
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", tmnCode);

            vnpParams.put("vnp_Amount", String.valueOf(totalAmount * 100));

            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", vnp_OrderInfo);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", locale);
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_IpAddr", clientIp);
            vnpParams.put("vnp_CreateDate", createDate);
            vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

            String vnp_SecureHash = vnpayHashUtil.hmacSHA512(vnpParams, hashSecret);

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                            .append('&');
                }
            }
            String queryUrl = query.substring(0, query.length() - 1);
            String paymentUrl = apiUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

            log.info("✅ VNPAY Order URL generated: {}", paymentUrl);
            return paymentUrl;

        } catch (Exception e) {
            log.error("❌ Error creating VNPAY Order URL", e);
            throw new RuntimeException("Cannot create VNPAY Order URL", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public VnpayIpnResponse handleVnpayIpn(Map<String, String> vnpParams) {
        try {
        String receivedHash = vnpParams.get("vnp_SecureHash");
        Map<String, String> hashCheckParams = new HashMap<>(vnpParams);
        hashCheckParams.remove("vnp_SecureHash");

        String calculatedHash = vnpayHashUtil.hmacSHA512(hashCheckParams, vnpayConfig.getHashSecret());
        if (!calculatedHash.equalsIgnoreCase(receivedHash)) {
            log.error("VNPAY IPN Hash Mismatch! TxnRef: {}", vnpParams.get("vnp_TxnRef"));
            return new VnpayIpnResponse("97", "Invalid Checksum");
        }

        String txnRef = vnpParams.get("vnp_TxnRef"); // Order ID
        long orderId;
        try {
            orderId = Long.parseLong(txnRef);
        } catch (NumberFormatException e) {
            return new VnpayIpnResponse("01", "Invalid Order ID format");
        }

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("Order not found: {}", orderId);
            return new VnpayIpnResponse("01", "Order not Found");
        }

        long vnpAmount = Long.parseLong(vnpParams.get("vnp_Amount")) / 100;
        if (vnpAmount != order.getTotalAmount().longValue()) {
            return new VnpayIpnResponse("04", "Invalid Amount");
        }

        if (OrderStatus.PAID.equals(order.getStatus())) {
            log.info("Order #{} already confirmed", orderId);
            return new VnpayIpnResponse("02", "Order already confirmed");
        }

        String responseCode = vnpParams.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {

            log.info("VNPAY IPN Success for Order #{}", orderId);

            // Grant access for all items - must succeed atomically
            try {
                for (OrderItemEntity item : order.getItems()) {
                    userAccessService.grantAccessDirectly(
                            order.getUserId(),
                            item.getCategoryCode(),
                            item.getDurationDays()
                    );
                }
            } catch (Exception e) {
                log.error("Failed to grant access for order #{}: {}", orderId, e.getMessage());
                throw new AppException(ErrorCode.INVALID_KEY,
                    "Failed to grant access for purchased items");
            }

            order.setStatus(OrderStatus.PAID);
            order.setTransactionNo(vnpParams.get("vnp_TransactionNo"));
            order.setBankCode(vnpParams.get("vnp_BankCode"));
            order.setPayDate(vnpParams.get("vnp_PayDate"));

            orderRepository.save(order);

            // Xóa giỏ hàng sau khi thanh toán thành công
            cartService.clearCart(order.getUserId());

            // Đánh dấu voucher đã sử dụng (nếu có)
            if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
                try {
                    voucherValidationService.markVoucherAsUsed(order.getUserId(), order.getVoucherCode());
                    log.info("Voucher {} marked as used for order #{}", order.getVoucherCode(), orderId);
                } catch (Exception e) {
                    log.warn("Failed to mark voucher as used: {}", e.getMessage());
                }
            }

            // Gửi thông báo thanh toán thành công
            notificationService.createNotification(
                order.getUserId(),
                "Thanh toán thành công",
                String.format("Đơn hàng #%d đã được thanh toán thành công. Số tiền: %,d VNĐ",
                    orderId, order.getTotalAmount().longValue()),
                NotificationType.PAYMENT_SUCCESS,
                "/orders/" + orderId
            );

            // Sync trạng thái về Firebase (nếu được cấu hình)
            syncOrderStatusToFirebase(orderId, "paid",
                    vnpParams.get("vnp_TransactionNo"),
                    vnpParams.get("vnp_BankCode"),
                    vnpParams.get("vnp_PayDate"));

        return new VnpayIpnResponse("00", "Confirm Success");

            } else {
                log.warn("VNPAY IPN Failed for Order #{}. Code: {}", orderId, responseCode);

                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);

                // Gửi thông báo thanh toán thất bại
                notificationService.createNotification(
                    order.getUserId(),
                    "Thanh toán thất bại",
                    String.format("Đơn hàng #%d thanh toán thất bại. Vui lòng thử lại hoặc liên hệ hỗ trợ.", orderId),
                    NotificationType.PAYMENT_FAILED,
                    "/orders/" + orderId
                );

                return new VnpayIpnResponse("00", "Confirm Success"); // Vẫn trả 00 để xác nhận đã nhận tin
            }

        } catch (Exception e) {
            log.error("Exception processing VNPAY IPN", e);
            return new VnpayIpnResponse("99", "Unknown Error");
        }
    }

    /**
     * Sync order status to Firebase (if configured).
     * Tìm Firebase order theo backend order ID và cập nhật trạng thái.
     */
    private void syncOrderStatusToFirebase(Long backendOrderId, String status,
                                           String transactionNo, String bankCode, String payDate) {
        if (firebaseOrderSyncService == null || !firebaseOrderSyncService.isFirebaseConfigured()) {
            log.debug("Firebase not configured. Skipping status sync for order #{}", backendOrderId);
            return;
        }

        try {
            // Tìm Firebase order ID từ Firestore theo backendOrderId
            // Vì order được sync từ Firebase, ta cần query ngược lại
            OrderEntity order = orderRepository.findById(backendOrderId).orElse(null);
            if (order == null) {
                return;
            }

            // Lấy tất cả orders của user và tìm order có backendOrderId match
            List<com.hth.udecareer.model.dto.FirebaseOrderDto> firebaseOrders =
                    firebaseOrderSyncService.getOrdersByUserId(order.getUserId());

            for (com.hth.udecareer.model.dto.FirebaseOrderDto fbOrder : firebaseOrders) {
                if (backendOrderId.equals(fbOrder.getBackendOrderId())) {
                    firebaseOrderSyncService.updateFirebaseOrderStatus(
                            fbOrder.getFirebaseOrderId(),
                            status,
                            transactionNo,
                            bankCode,
                            payDate
                    );
                    log.info("Synced order #{} status to Firebase order {}",
                            backendOrderId, fbOrder.getFirebaseOrderId());
                    return;
                }
            }

            log.debug("No Firebase order found for backend order #{}", backendOrderId);

        } catch (Exception e) {
            log.warn("Failed to sync order status to Firebase: {}", e.getMessage());
            // Don't fail the IPN processing if Firebase sync fails
        }
    }
}