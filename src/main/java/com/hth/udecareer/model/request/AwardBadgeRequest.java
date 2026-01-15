package com.hth.udecareer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Award badge to user request")
public class AwardBadgeRequest {

    @Schema(description = "User ID to award badge to", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("user_id")
    @NotNull(message = "User ID is required")
    private Long userId;

    @Schema(description = "Badge ID to award", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("badge_id")
    @NotNull(message = "Badge ID is required")
    private Long badgeId;

    @Schema(description = "Optional note about the award", example = "Special recognition for community contribution")
    private String note;
}
