package com.hth.udecareer.eil.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for AI explanation of a question")
public class ExplainRequest {

    @NotNull(message = "Question ID is required")
    @Schema(description = "Question ID to explain", example = "123")
    private Long questionId;

    @NotNull(message = "User answer is required")
    @Schema(description = "User's answer as array of booleans", example = "[false, true, false, false]")
    private List<Boolean> userAnswer;

    @Schema(description = "Language for explanation: vi (Vietnamese) or en (English)",
            example = "vi", defaultValue = "vi")
    @Builder.Default
    private String language = "vi";

    @Schema(description = "Detail level: BRIEF, NORMAL, DETAILED",
            example = "NORMAL", defaultValue = "NORMAL")
    @Builder.Default
    private String detailLevel = "NORMAL";
}
