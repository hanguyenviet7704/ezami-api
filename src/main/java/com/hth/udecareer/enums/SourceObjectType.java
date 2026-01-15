package com.hth.udecareer.enums;


public enum SourceObjectType {
    ORDER("order"),
    QUIZ("quiz"),
    COURSE("course"),
    COMMENT("comment"),
    FEED("feed");

    private final String value;

    SourceObjectType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SourceObjectType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        for (SourceObjectType type : SourceObjectType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        try {
            return SourceObjectType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
