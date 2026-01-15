package com.hth.udecareer.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO để map dữ liệu order từ Firestore.
 * Cấu trúc phù hợp với cả Web và Mobile App.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseOrderDto {

    /**
     * Firebase document ID (unique identifier trong Firestore)
     */
    private String firebaseOrderId;

    /**
     * User ID từ Backend (wp_users.ID)
     */
    private Long userId;

    /**
     * Email người dùng (dùng để lookup userId nếu cần)
     */
    private String userEmail;

    /**
     * Tổng tiền sau giảm giá
     */
    private BigDecimal totalAmount;

    /**
     * Tổng tiền trước giảm giá
     */
    private BigDecimal originalAmount;

    /**
     * Số tiền được giảm
     */
    private BigDecimal discountAmount;

    /**
     * Mã voucher đã áp dụng
     */
    private String voucherCode;

    /**
     * Trạng thái order: pending, paid, failed, cancelled, expired, refunded
     */
    private String status;

    /**
     * Phương thức thanh toán: VNPAY, REVENUECAT, BANK_TRANSFER
     */
    private String paymentMethod;

    /**
     * Mã giao dịch từ cổng thanh toán
     */
    private String transactionNo;

    /**
     * Mã ngân hàng
     */
    private String bankCode;

    /**
     * Ngày thanh toán (format: yyyyMMddHHmmss)
     */
    private String payDate;

    /**
     * Nguồn tạo order: WEB, IOS, ANDROID
     */
    private String source;

    /**
     * Timestamp tạo order (milliseconds)
     */
    private Long createdAt;

    /**
     * Timestamp cập nhật order (milliseconds)
     */
    private Long updatedAt;

    /**
     * Đã sync về Backend chưa
     */
    private Boolean synced;

    /**
     * Backend order ID sau khi sync
     */
    private Long backendOrderId;

    /**
     * Danh sách items trong order
     */
    private List<FirebaseOrderItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirebaseOrderItemDto {
        private String categoryCode;
        private String planCode;
        private BigDecimal price;
        private Integer durationDays;
        private String categoryName;
        private String planName;
    }
}
