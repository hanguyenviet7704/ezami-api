package com.hth.udecareer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Report submission request")
public class ReportRequest {

    @Schema(description = "ID of the object being reported", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("object_id")
    @NotNull(message = "Object ID is required")
    private Long objectId;

    @Schema(description = "Type of object being reported: feed, comment, user, space", example = "feed", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("object_type")
    @NotBlank(message = "Object type is required")
    private String objectType;

    @Schema(description = "Reason for reporting: spam, harassment, hate_speech, violence, inappropriate, other", example = "spam", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Reason is required")
    private String reason;

    @Schema(description = "Additional description or context for the report", example = "This post contains spam links")
    private String description;
}
