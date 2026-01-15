package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User points with ranking information")
public class UserPointsWithRankResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "Display name", example = "John Doe")
    @JsonProperty("display_name")
    private String displayName;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    @JsonProperty("avatar_url")
    private String avatarUrl;

    @Schema(description = "Current total points", example = "1500")
    @JsonProperty("current_points")
    private Long currentPoints;

    @Schema(description = "Points earned this week", example = "100")
    @JsonProperty("week_points")
    private Long weekPoints;

    @Schema(description = "Points earned this month", example = "500")
    @JsonProperty("month_points")
    private Long monthPoints;

    @Schema(description = "Points earned this year", example = "1500")
    @JsonProperty("year_points")
    private Long yearPoints;

    @Schema(description = "User's rank in weekly leaderboard", example = "5")
    @JsonProperty("week_rank")
    private Integer weekRank;

    @Schema(description = "User's rank in monthly leaderboard", example = "8")
    @JsonProperty("month_rank")
    private Integer monthRank;

    @Schema(description = "User's rank in yearly leaderboard", example = "12")
    @JsonProperty("year_rank")
    private Integer yearRank;
}

