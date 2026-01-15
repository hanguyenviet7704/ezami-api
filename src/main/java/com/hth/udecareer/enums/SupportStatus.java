package com.hth.udecareer.enums;

import lombok.Getter;

@Getter
public enum SupportStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    RESOLVED("resolved"),
    REJECTED("rejected"),
    LOG_ONLY("log_only");

    private final String value;

    SupportStatus(String value) {
        this.value = value;
    }
}
