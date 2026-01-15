package com.hth.udecareer.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LeaderBoardResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private Long totalPoints;
    private Integer rank;
}
