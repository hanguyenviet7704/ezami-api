package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionType {
    ADAPTIVE("ADAPTIVE", "Adaptive Practice", "Luyện tập thích ứng"),
    SKILL_FOCUS("SKILL_FOCUS", "Skill Focus", "Tập trung kỹ năng"),
    REVIEW("REVIEW", "Review", "Ôn tập"),
    MIXED("MIXED", "Mixed Practice", "Luyện tập tổng hợp"),
    DIAGNOSTIC("DIAGNOSTIC", "Diagnostic Test", "Bài kiểm tra chẩn đoán");

    private final String code;
    private final String nameEn;
    private final String nameVi;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized session type
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static SessionType fromCode(String code) {
        for (SessionType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return ADAPTIVE; // Default to adaptive
    }
}
