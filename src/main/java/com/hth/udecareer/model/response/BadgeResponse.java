package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Badge response")
public class BadgeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Badge ID", example = "1")
    private Long id;

    @Schema(description = "Badge name", example = "First Post")
    private String name;

    @Schema(description = "Badge slug", example = "first-post")
    private String slug;

    @Schema(description = "Badge description", example = "Earned by creating your first post")
    private String description;

    @Schema(description = "Badge icon URL")
    private String icon;

    @Schema(description = "Badge color code", example = "#FFD700")
    private String color;

    @Schema(description = "Badge type", example = "achievement")
    private String type;

    @Schema(description = "Requirement type", example = "posts")
    @JsonProperty("requirement_type")
    private String requirementType;

    @Schema(description = "Requirement value", example = "1")
    @JsonProperty("requirement_value")
    private Integer requirementValue;

    @Schema(description = "Points rewarded", example = "10")
    @JsonProperty("points_reward")
    private Integer pointsReward;

    @Schema(description = "Is badge active", example = "true")
    @JsonProperty("is_active")
    private Boolean isActive;

    @Schema(description = "Display priority", example = "0")
    private Integer priority;

    @Schema(description = "Number of users who have this badge", example = "150")
    @JsonProperty("earned_count")
    private Long earnedCount;

    @Schema(description = "Created timestamp")
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
}
