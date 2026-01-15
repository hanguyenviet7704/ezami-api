package com.hth.udecareer.enums;

public enum OrderStatus {
    /**
     * Đơn hàng mới tạo, đang chờ thanh toán.
     */
    PENDING,

    /**
     * Thanh toán thành công, đã cấp quyền.
     */
    PAID,

    /**
     * Thanh toán thất bại (lỗi thẻ, lỗi mạng...).
     */
    FAILED,

    /**
     * Người dùng chủ động hủy giao dịch tại cổng VNPAY.
     */
    CANCELLED,

    /**
     * (Tùy chọn) Đơn hàng để quá lâu không thanh toán.
     */
    EXPIRED,

    /**
     * Đã hoàn tiền thành công, quyền truy cập đã bị thu hồi.
     */
    REFUNDED
}