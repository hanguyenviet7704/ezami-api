package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Follow action response")
public class FollowResponse {

    @Schema(description = "Follow relationship ID", example = "1")
    private Long id;

    @Schema(description = "Follower user ID", example = "10")
    @JsonProperty("follower_id")
    private Long followerId;

    @Schema(description = "Followed user ID", example = "20")
    @JsonProperty("followed_id")
    private Long followedId;

    @Schema(description = "Follow level: 0=blocked, 1=following, 2=following with notifications", example = "1")
    private Integer level;

    @Schema(description = "Profile of the target user")
    private XProfileResponse xprofile;

    @Schema(description = "Action result message", example = "Successfully followed user")
    private String message;

    @Schema(description = "Whether the action was successful", example = "true")
    private Boolean success;
}
