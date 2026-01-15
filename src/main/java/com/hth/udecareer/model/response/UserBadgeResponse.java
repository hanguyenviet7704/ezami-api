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
@Schema(description = "User badge response")
public class UserBadgeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User badge record ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "100")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "User profile")
    private XProfileResponse xprofile;

    @Schema(description = "Badge ID", example = "1")
    @JsonProperty("badge_id")
    private Long badgeId;

    @Schema(description = "Badge details")
    private BadgeResponse badge;

    @Schema(description = "Is badge featured on profile", example = "false")
    @JsonProperty("is_featured")
    private Boolean isFeatured;

    @Schema(description = "Admin who awarded (for manual badges)", example = "1")
    @JsonProperty("awarded_by")
    private Long awardedBy;

    @Schema(description = "Award note", example = "Special recognition")
    private String note;

    @Schema(description = "When badge was earned")
    @JsonProperty("earned_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime earnedAt;
}
