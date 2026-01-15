package com.hth.udecareer.model.response;

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
@Schema(description = "Leaderboard response containing multiple time period sections")
public class LeaderboardListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "List of leaderboard sections (7 days, 30 days, all time)")
    private List<LeaderboardSectionResponse> leaderboard;
}
