package com.hth.udecareer.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderBoardWithUserPageResponse {
    private PageResponse<LeaderBoardResponse> topUsers;
    private UserRankDetail myRank;
    private String updateTime;
}

