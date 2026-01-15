package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Streak goal response")
public class StreakGoalResponse {

    private Long id;

    @JsonProperty("goal_type")
    private String goalType;

    private String code;
    private String name;
    private String description;
    private String icon;

    @JsonProperty("requirement_json")
    private String requirementJson;

    @JsonProperty("reward_json")
    private String rewardJson;

    @JsonProperty("is_repeatable")
    private Boolean isRepeatable;

    @JsonProperty("user_status")
    private String userStatus; // ACTIVE, COMPLETED, CLAIMED, EXPIRED

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}
