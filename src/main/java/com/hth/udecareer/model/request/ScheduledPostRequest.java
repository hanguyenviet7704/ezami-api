package com.hth.udecareer.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Scheduled post request")
public class ScheduledPostRequest {

    @Schema(description = "Post title (optional)")
    private String title;

    @Schema(description = "Post message content", example = "This is my scheduled post!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Message is required")
    private String message;

    @Schema(description = "Space ID to post to")
    @JsonProperty("space_id")
    private Long spaceId;

    @Schema(description = "List of media URLs")
    private List<String> media;

    @Schema(description = "List of topic IDs")
    @JsonProperty("topic_ids")
    private List<Long> topicIds;

    @Schema(description = "Post privacy: public, followers, private", example = "public")
    private String privacy;

    @Schema(description = "Scheduled publication time", example = "2024-12-20 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("scheduled_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;
}
