package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MasteryLabel {
    WEAK("WEAK", "Weak", "Yếu", 0.0, 0.25),
    DEVELOPING("DEVELOPING", "Developing", "Đang phát triển", 0.25, 0.50),
    PROFICIENT("PROFICIENT", "Proficient", "Thành thạo", 0.50, 0.75),
    STRONG("STRONG", "Strong", "Mạnh", 0.75, 1.0);

    private final String code;
    private final String nameEn;
    private final String nameVi;
    private final double minLevel;
    private final double maxLevel;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized mastery label
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static MasteryLabel fromLevel(double mastery) {
        if (mastery < 0.25) return WEAK;
        if (mastery < 0.50) return DEVELOPING;
        if (mastery < 0.75) return PROFICIENT;
        return STRONG;
    }

    public boolean isWeak() {
        return this == WEAK || this == DEVELOPING;
    }
}
