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
@Schema(description = "Report response")
public class ReportResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Report ID", example = "1")
    private Long id;

    @Schema(description = "Reporter user ID", example = "100")
    @JsonProperty("reporter_id")
    private Long reporterId;

    @Schema(description = "Reporter profile")
    private XProfileResponse reporter;

    @Schema(description = "Reported user ID (if reporting a user)", example = "200")
    @JsonProperty("reported_user_id")
    private Long reportedUserId;

    @Schema(description = "Reported user profile")
    @JsonProperty("reported_user")
    private XProfileResponse reportedUser;

    @Schema(description = "ID of the reported object", example = "123")
    @JsonProperty("object_id")
    private Long objectId;

    @Schema(description = "Type of reported object: feed, comment, user, space", example = "feed")
    @JsonProperty("object_type")
    private String objectType;

    @Schema(description = "Report reason", example = "spam")
    private String reason;

    @Schema(description = "Additional description", example = "This post contains spam links")
    private String description;

    @Schema(description = "Report status: pending, reviewing, resolved, dismissed", example = "pending")
    private String status;

    @Schema(description = "Moderator user ID who handled the report", example = "1")
    @JsonProperty("moderator_id")
    private Long moderatorId;

    @Schema(description = "Moderator profile")
    private XProfileResponse moderator;

    @Schema(description = "Moderator notes", example = "Content removed for violating guidelines")
    @JsonProperty("moderator_notes")
    private String moderatorNotes;

    @Schema(description = "Action taken: none, content_removed, user_warned, user_banned", example = "content_removed")
    @JsonProperty("action_taken")
    private String actionTaken;

    @Schema(description = "Number of reports for this object", example = "3")
    @JsonProperty("report_count")
    private Long reportCount;

    @Schema(description = "Resolved timestamp")
    @JsonProperty("resolved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime resolvedAt;

    @Schema(description = "Created timestamp")
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
}
