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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Scheduled post response")
public class ScheduledPostResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Scheduled post ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "100")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "User profile")
    private XProfileResponse xprofile;

    @Schema(description = "Space ID", example = "1")
    @JsonProperty("space_id")
    private Long spaceId;

    @Schema(description = "Space name", example = "General Discussion")
    @JsonProperty("space_name")
    private String spaceName;

    @Schema(description = "Post title")
    private String title;

    @Schema(description = "Post message content")
    private String message;

    @Schema(description = "List of media URLs")
    private List<String> media;

    @Schema(description = "List of topic IDs")
    @JsonProperty("topic_ids")
    private List<Long> topicIds;

    @Schema(description = "Post privacy", example = "public")
    private String privacy;

    @Schema(description = "Scheduled post status: scheduled, published, cancelled, failed", example = "scheduled")
    private String status;

    @Schema(description = "Scheduled publication time")
    @JsonProperty("scheduled_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime scheduledAt;

    @Schema(description = "Actual publication time (if published)")
    @JsonProperty("published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime publishedAt;

    @Schema(description = "Published post ID (if published)")
    @JsonProperty("published_post_id")
    private Long publishedPostId;

    @Schema(description = "Error message (if failed)")
    @JsonProperty("error_message")
    private String errorMessage;

    @Schema(description = "Created timestamp")
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
}
