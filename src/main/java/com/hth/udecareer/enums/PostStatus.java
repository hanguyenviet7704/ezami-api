package com.hth.udecareer.enums;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostStatus {
    DRAFT("draft"),
    TRASH("trash"),
    PRIVATE("private"),
    PUBLISH("publish"),
    FUTURE("future");

    private final String value;

    @Nullable
    public static PostStatus getByValue(String value) {
        for (PostStatus type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
