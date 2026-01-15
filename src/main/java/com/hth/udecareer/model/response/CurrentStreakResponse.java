package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current streak response")
public class CurrentStreakResponse {

    @Schema(description = "Current active streak days", example = "15")
    @JsonProperty("current_streak")
    private Integer currentStreak;

    @Schema(description = "Longest streak ever achieved", example = "45")
    @JsonProperty("longest_streak")
    private Integer longestStreak;

    @Schema(description = "Available freeze count", example = "2")
    @JsonProperty("freeze_count")
    private Integer freezeCount;

    @Schema(description = "Total freezes used in lifetime", example = "5")
    @JsonProperty("freeze_used_count")
    private Integer freezeUsedCount;

    @Schema(description = "Total days active in lifetime", example = "120")
    @JsonProperty("total_days_active")
    private Integer totalDaysActive;

    @Schema(description = "Last activity date", example = "2026-01-06")
    @JsonProperty("last_activity_date")
    private LocalDate lastActivityDate;

    @Schema(description = "Current streak start date", example = "2025-12-22")
    @JsonProperty("streak_start_date")
    private LocalDate streakStartDate;
}
