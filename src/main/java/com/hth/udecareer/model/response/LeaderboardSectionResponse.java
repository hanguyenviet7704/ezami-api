package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Leaderboard section for a specific time period")
public class LeaderboardSectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Section title", example = "Last 7 days")
    private String title;

    @Schema(description = "Section key identifier", example = "7_days")
    private String key;

    @Schema(description = "List of leaderboard items")
    private List<LeaderboardItemResponse> items;

    @Schema(description = "Total number of users in this period")
    @JsonProperty("total_users")
    private Long totalUsers;
}
