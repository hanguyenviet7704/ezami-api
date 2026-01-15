package com.hth.udecareer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Badge create/update request")
public class BadgeRequest {

    @Schema(description = "Badge ID for update (optional for create)")
    private Long id;

    @Schema(description = "Badge name", example = "First Post", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Badge slug (auto-generated if not provided)", example = "first-post")
    private String slug;

    @Schema(description = "Badge description", example = "Earned by creating your first post")
    private String description;

    @Schema(description = "Badge icon URL")
    private String icon;

    @Schema(description = "Badge color code", example = "#FFD700")
    private String color;

    @Schema(description = "Badge type: achievement, milestone, special, custom", example = "achievement")
    private String type;

    @Schema(description = "Requirement type: posts, comments, reactions, followers, points, manual", example = "posts")
    @JsonProperty("requirement_type")
    private String requirementType;

    @Schema(description = "Value needed to earn badge", example = "1")
    @JsonProperty("requirement_value")
    private Integer requirementValue;

    @Schema(description = "Points rewarded when badge is earned", example = "10")
    @JsonProperty("points_reward")
    private Integer pointsReward;

    @Schema(description = "Whether badge is active", example = "true")
    @JsonProperty("is_active")
    private Boolean isActive;

    @Schema(description = "Display priority (lower = higher priority)", example = "0")
    private Integer priority;
}
