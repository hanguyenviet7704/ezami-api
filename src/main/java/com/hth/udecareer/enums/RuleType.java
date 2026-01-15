package com.hth.udecareer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuleType {
    GLOBAL("global"),
    PRODUCT("product"),
    CATEGORY("category"),
    AFFILIATE("affiliate"),
    TIER("tier");

    private final String value;

    RuleType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RuleType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RuleType type : RuleType.values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown RuleType: " + value);
    }
}




