package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Streak update response")
public class StreakUpdateResponse {

    @Schema(description = "Current streak after update", example = "16")
    @JsonProperty("current_streak")
    private Integer currentStreak;

    @Schema(description = "Longest streak", example = "45")
    @JsonProperty("longest_streak")
    private Integer longestStreak;

    @Schema(description = "Remaining freeze count", example = "1")
    @JsonProperty("freeze_count")
    private Integer freezeCount;

    @Schema(description = "Was streak broken", example = "false")
    @JsonProperty("streak_broken")
    private Boolean streakBroken;

    @Schema(description = "Was freeze auto-used to save streak", example = "true")
    @JsonProperty("freeze_auto_used")
    private Boolean freezeAutoUsed;

    @Schema(description = "Rewards earned from milestones")
    private List<String> rewards;

    @Schema(description = "Last activity date", example = "2026-01-06")
    @JsonProperty("last_activity_date")
    private LocalDate lastActivityDate;
}
