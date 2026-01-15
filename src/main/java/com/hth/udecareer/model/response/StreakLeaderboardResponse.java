package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Streak leaderboard response")
public class StreakLeaderboardResponse {

    private List<StreakLeaderboardItemResponse> items;
    private Integer page;
    private Integer size;
}
