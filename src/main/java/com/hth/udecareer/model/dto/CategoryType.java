package com.hth.udecareer.model.dto;

import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    DEFAULT(""),
    TOEIC("TOEIC"),
    JLPT("JLPT");

    @JsonValue
    private final String code;

    @Nullable
    @JsonCreator
    public static CategoryType fromJsonValue(@Nullable final String jsonValue) {
        if (Objects.isNull(jsonValue)) {
            return null;
        }
        for (final CategoryType type : values()) {
            if (type.getCode().equalsIgnoreCase(jsonValue)) {
                return type;
            }
        }
        return null;
    }
}
