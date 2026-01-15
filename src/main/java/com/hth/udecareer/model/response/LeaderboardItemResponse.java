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
@Schema(description = "Leaderboard item representing a user's ranking")
public class LeaderboardItemResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Total points earned in the period", example = "150")
    @JsonProperty("total_points")
    private Integer totalPoints;

    @Schema(description = "User's rank in the leaderboard", example = "1")
    private Integer rank;

    @Schema(description = "User profile information")
    private XProfileResponse xprofile;
}
