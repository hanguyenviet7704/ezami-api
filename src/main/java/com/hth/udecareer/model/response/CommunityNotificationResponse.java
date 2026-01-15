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
@Schema(description = "Community notification response")
public class CommunityNotificationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "Notification action type", example = "comment_added")
    private String action;

    @Schema(description = "Notification title", example = "New comment")
    private String title;

    @Schema(description = "Notification content", example = "John commented on your post")
    private String content;

    @Schema(description = "Deep link route")
    private String route;

    @Schema(description = "Whether the notification has been read", example = "false")
    @JsonProperty("is_read")
    private Boolean isRead;

    @Schema(description = "Related feed ID")
    @JsonProperty("feed_id")
    private Long feedId;

    @Schema(description = "Source user ID (who triggered the notification)")
    @JsonProperty("src_user_id")
    private Long srcUserId;

    @Schema(description = "Source user profile")
    @JsonProperty("src_user")
    private XProfileResponse srcUser;

    @Schema(description = "Created at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "Read at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @JsonProperty("read_at")
    private LocalDateTime readAt;
}
