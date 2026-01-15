package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstimatedLevel {
    BEGINNER("BEGINNER", "Beginner", "Mới bắt đầu", 0, 250, 0.0, 0.2),
    ELEMENTARY("ELEMENTARY", "Elementary", "Sơ cấp", 250, 400, 0.2, 0.4),
    INTERMEDIATE("INTERMEDIATE", "Intermediate", "Trung cấp", 400, 600, 0.4, 0.6),
    UPPER_INTERMEDIATE("UPPER_INTERMEDIATE", "Upper Intermediate", "Trung cấp cao", 600, 785, 0.6, 0.8),
    ADVANCED("ADVANCED", "Advanced", "Cao cấp", 785, 990, 0.8, 1.0);

    private final String code;
    private final String nameEn;
    private final String nameVi;
    private final int minScore;
    private final int maxScore;
    private final double minMastery;
    private final double maxMastery;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized level name
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static EstimatedLevel fromScore(int score) {
        for (EstimatedLevel level : values()) {
            if (score >= level.minScore && score < level.maxScore) {
                return level;
            }
        }
        return score >= 785 ? ADVANCED : BEGINNER;
    }

    public static EstimatedLevel fromMastery(double mastery) {
        for (EstimatedLevel level : values()) {
            if (mastery >= level.minMastery && mastery < level.maxMastery) {
                return level;
            }
        }
        return mastery >= 0.8 ? ADVANCED : BEGINNER;
    }

    public int getEstimatedScore(double mastery) {
        double normalizedMastery = (mastery - minMastery) / (maxMastery - minMastery);
        return (int) (minScore + normalizedMastery * (maxScore - minScore));
    }
}
