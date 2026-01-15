package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile information")
public class XProfileResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Username", example = "admin2")
    private String username;

    @Schema(description = "Display name", example = "a a")
    @JsonProperty("display_name")
    private String displayName;

    @Schema(description = "Full name", example = "Nguyễn Văn A")
    @JsonProperty("full_name")
    private String fullName;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "Gender", example = "Nam")
    private String gender;

    @Schema(description = "Total points", example = "3")
    @JsonProperty("total_points")
    private Integer totalPoints;

    @Schema(description = "Is verified", example = "false")
    @JsonProperty("is_verified")
    private Boolean isVerified;

    // Follow information (optional - only when viewing other's profile)
    @Schema(description = "Whether current user is following this user", example = "false")
    @JsonProperty("is_following")
    private Boolean isFollowing;

    @Schema(description = "Whether current user has blocked this user", example = "false")
    @JsonProperty("is_blocked")
    private Boolean isBlocked;

    @Schema(description = "Whether this user follows current user", example = "false")
    @JsonProperty("follows_me")
    private Boolean followsMe;

    // Counts (optional - for public profile view)
    @Schema(description = "Number of followers", example = "100")
    @JsonProperty("followers_count")
    private Long followersCount;

    @Schema(description = "Number of followings", example = "50")
    @JsonProperty("followings_count")
    private Long followingsCount;
}

