package com.hth.udecareer.enums;

import lombok.Getter;

@Getter
public enum FavoritableType {
    COURSE("course"),
    LESSON("lesson"),
    QUIZ("quiz"),
    TOPIC("topic"),
    POST("post"),
    CNOTE("cnote");

    private final String value;

    FavoritableType(String value) {
        this.value = value;
    }

    public static FavoritableType fromString(String value) {
        for (FavoritableType type : FavoritableType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown favoritable type: " + value);
    }
}
