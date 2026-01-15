package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Follow list item")
public class FollowItemResponse {

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

    @Schema(description = "User profile (follower or followed depending on context)")
    private XProfileResponse user;

    @Schema(description = "Current user's follow status to this user: null=not following, 1=following, 2=following with notifications", example = "1")
    @JsonProperty("my_follow_status")
    private Integer myFollowStatus;

    @Schema(description = "Whether this user follows the current user back", example = "true")
    @JsonProperty("follows_me")
    private Boolean followsMe;

    @Schema(description = "When the follow relationship was created")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
