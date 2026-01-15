package com.hth.udecareer.model.response;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaderBoardWithUserResponse {

    private List<LeaderBoardResponse> topUsers;

    private UserRankDetail myRank;

    private  String updateTime;
}
