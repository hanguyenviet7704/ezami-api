package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillCategory {
    LISTENING("LISTENING", "Listening", "Nghe"),
    READING("READING", "Reading", "Đọc"),
    GRAMMAR("GRAMMAR", "Grammar", "Ngữ pháp"),
    VOCABULARY("VOCABULARY", "Vocabulary", "Từ vựng");

    private final String code;
    private final String nameEn;
    private final String nameVi;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized category name
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static SkillCategory fromCode(String code) {
        for (SkillCategory category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        return null;
    }
}
