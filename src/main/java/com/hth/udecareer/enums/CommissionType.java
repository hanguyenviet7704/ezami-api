package com.hth.udecareer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommissionType {
    PERCENTAGE("percentage"),
    FIXED("fixed"),
    HYBRID("hybrid");

    private final String value;

    CommissionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CommissionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CommissionType type : CommissionType.values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown CommissionType: " + value);
    }
}




