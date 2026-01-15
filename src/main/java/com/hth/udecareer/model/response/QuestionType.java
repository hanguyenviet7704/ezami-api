package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    NORMAL(0),
    PART(1),
    GROUP(2);

    @JsonValue
    private final int value;

    @Nullable
    @JsonCreator
    public static QuestionType fromJsonValue(@Nullable final Integer jsonValue) {
        if (Objects.isNull(jsonValue)) {
            return null;
        }
        for (final QuestionType type : values()) {
            if (type.getValue() == jsonValue) {
                return type;
            }
        }
        return null;
    }
}
