package com.hth.udecareer.enums;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuizType {
    FULL("full"),
    HALF("half"),
    MINI("mini"),
    PART("part");

    @JsonValue
    private final String value;

    @Nullable
    public static QuizType getByValue(String value) {
        for (QuizType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
