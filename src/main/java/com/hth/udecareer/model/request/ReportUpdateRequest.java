package com.hth.udecareer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Report update request for moderators")
public class ReportUpdateRequest {

    @Schema(description = "New status: pending, reviewing, resolved, dismissed", example = "resolved", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status is required")
    private String status;

    @Schema(description = "Action taken: none, content_removed, user_warned, user_banned", example = "content_removed")
    @JsonProperty("action_taken")
    private String actionTaken;

    @Schema(description = "Moderator notes about the resolution", example = "Content was removed for violating community guidelines")
    @JsonProperty("moderator_notes")
    private String moderatorNotes;
}
