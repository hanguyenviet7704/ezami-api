package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DifficultyLevel {
    VERY_EASY(1, "Very Easy", "Rất dễ", 0.5),
    EASY(2, "Easy", "Dễ", 0.75),
    MEDIUM(3, "Medium", "Trung bình", 1.0),
    HARD(4, "Hard", "Khó", 1.25),
    VERY_HARD(5, "Very Hard", "Rất khó", 1.5);

    private final int level;
    private final String nameEn;
    private final String nameVi;
    private final double weight;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized difficulty level name
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static DifficultyLevel fromLevel(int level) {
        for (DifficultyLevel difficulty : values()) {
            if (difficulty.level == level) {
                return difficulty;
            }
        }
        return MEDIUM; // Default to medium
    }

    public static DifficultyLevel fromMastery(double mastery) {
        if (mastery < 0.2) return VERY_EASY;
        if (mastery < 0.4) return EASY;
        if (mastery < 0.6) return MEDIUM;
        if (mastery < 0.8) return HARD;
        return VERY_HARD;
    }
}
