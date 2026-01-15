package com.hth.udecareer.enums;

import lombok.Getter;

@Getter
public enum FavoriteStatus {
    ACTIVE("active"),
    DELETED("deleted");

    private final String value;

    FavoriteStatus(String value) {
        this.value = value;
    }

    public static FavoriteStatus fromString(String value) {
        for (FavoriteStatus status : FavoriteStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown favorite status: " + value);
    }
}
