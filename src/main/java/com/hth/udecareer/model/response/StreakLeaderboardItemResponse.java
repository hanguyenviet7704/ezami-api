package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Leaderboard item")
public class StreakLeaderboardItemResponse {

    private Integer rank;

    @JsonProperty("user_id")
    private Long userId;

    private String username;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("longest_streak")
    private Integer longestStreak;

    @JsonProperty("current_streak")
    private Integer currentStreak;
}
