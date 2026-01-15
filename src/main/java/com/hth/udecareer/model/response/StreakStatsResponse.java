package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Streak statistics response")
public class StreakStatsResponse {

    @Schema(description = "Current streak", example = "15")
    @JsonProperty("current_streak")
    private Integer currentStreak;

    @Schema(description = "Longest streak", example = "45")
    @JsonProperty("longest_streak")
    private Integer longestStreak;

    @Schema(description = "Total days active", example = "120")
    @JsonProperty("total_days_active")
    private Integer totalDaysActive;

    @Schema(description = "Freeze count", example = "2")
    @JsonProperty("freeze_count")
    private Integer freezeCount;

    @Schema(description = "Freeze used count", example = "5")
    @JsonProperty("freeze_used_count")
    private Integer freezeUsedCount;

    @Schema(description = "User rank by longest streak", example = "23")
    private Integer rank;

    @Schema(description = "Streak start date", example = "2025-12-22")
    @JsonProperty("streak_start_date")
    private LocalDate streakStartDate;

    @Schema(description = "Last activity date", example = "2026-01-06")
    @JsonProperty("last_activity_date")
    private LocalDate lastActivityDate;
}
