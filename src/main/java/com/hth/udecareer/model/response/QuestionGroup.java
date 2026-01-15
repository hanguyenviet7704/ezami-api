package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionGroup {
    LISTENING("L"),
    READING("R");

    @JsonValue
    private final String value;
}
