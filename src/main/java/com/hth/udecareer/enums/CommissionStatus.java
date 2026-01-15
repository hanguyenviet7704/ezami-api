package com.hth.udecareer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Commission status for affiliate referrals
 * This is an alias for ReferralStatus to maintain consistency
 */
public enum CommissionStatus {
    PENDING("pending"),       // Chờ xử lý
    APPROVED("approved"),     // Đã duyệt
    REJECTED("rejected"),     // Từ chối
    PAID("paid"),            // Đã thanh toán
    CANCELLED("cancelled");  // Đã hủy

    private final String value;

    CommissionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CommissionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CommissionStatus status : CommissionStatus.values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CommissionStatus: " + value);
    }
}

