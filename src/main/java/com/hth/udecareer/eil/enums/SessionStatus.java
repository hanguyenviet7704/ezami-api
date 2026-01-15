package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionStatus {
    IN_PROGRESS("IN_PROGRESS", "In Progress", "Đang thực hiện"),
    ACTIVE("ACTIVE", "Active", "Đang hoạt động"),
    PAUSED("PAUSED", "Paused", "Tạm dừng"),
    COMPLETED("COMPLETED", "Completed", "Hoàn thành"),
    ABANDONED("ABANDONED", "Abandoned", "Đã hủy");

    private final String code;
    private final String nameEn;
    private final String nameVi;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized session status
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    public static SessionStatus fromCode(String code) {
        for (SessionStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean isActive() {
        return this == IN_PROGRESS || this == ACTIVE;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == ABANDONED;
    }
}
