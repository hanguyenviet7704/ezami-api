package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackType {
    EXPLANATION("EXPLANATION", "Question Explanation", "Giải thích câu hỏi"),
    RECOMMENDATION("RECOMMENDATION", "Study Recommendation", "Đề xuất học tập"),
    SUMMARY("SUMMARY", "Session Summary", "Tóm tắt phiên học"),
    STUDY_PLAN("STUDY_PLAN", "Study Plan", "Kế hoạch học tập"),
    PROGRESS_REPORT("PROGRESS_REPORT", "Progress Report", "Báo cáo tiến độ");

    private final String code;
    private final String nameEn;
    private final String nameVi;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized feedback type
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static FeedbackType fromCode(String code) {
        for (FeedbackType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return EXPLANATION;
    }
}
