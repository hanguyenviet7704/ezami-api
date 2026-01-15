package com.hth.udecareer.eil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TestType {
    TOEIC("TOEIC", "TOEIC", 990, 10, 990),
    IELTS("IELTS", "IELTS", 9, 0, 9),
    TOEFL("TOEFL", "TOEFL iBT", 120, 0, 120);

    private final String code;
    private final String name;
    private final int maxScore;
    private final int minScore;
    private final int passScore;

    public static TestType fromCode(String code) {
        for (TestType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return TOEIC; // Default to TOEIC
    }
}
