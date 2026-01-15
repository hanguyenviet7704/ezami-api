package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Goal claim response")
public class GoalClaimResponse {

    @JsonProperty("goal_id")
    private Long goalId;

    @JsonProperty("goal_name")
    private String goalName;

    @JsonProperty("rewards_granted")
    private List<String> rewardsGranted;

    @JsonProperty("claimed_at")
    private LocalDateTime claimedAt;
}
