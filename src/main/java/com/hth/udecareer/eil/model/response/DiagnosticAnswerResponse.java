package com.hth.udecareer.eil.model.response;

import com.hth.udecareer.model.response.QuestionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after submitting a diagnostic answer")
public class DiagnosticAnswerResponse {

    @Schema(description = "Whether the answer was correct", example = "true")
    private Boolean isCorrect;

    @Schema(description = "Number of questions answered", example = "5")
    private Integer questionsAnswered;

    @Schema(description = "Number of questions remaining", example = "25")
    private Integer questionsRemaining;

    @Schema(description = "Next question to answer (null if diagnostic is finished)")
    private QuestionResponse nextQuestion;

    @Schema(description = "Current progress (0.0 - 1.0)", example = "0.17")
    private Double currentProgress;

    @Schema(description = "Session auto-terminated due to early stop conditions", example = "false")
    private Boolean autoTerminated;

    @Schema(description = "Reason for termination", example = "2 consecutive wrong in same skill")
    private String terminationReason;

    @Schema(description = "Consecutive wrong answers overall", example = "2")
    private Integer consecutiveWrong;

    @Schema(description = "Consecutive wrong answers in current skill", example = "1")
    private Integer skillConsecutiveWrong;

    @Schema(description = "Current skill being tested")
    private String currentSkillName;

    @Schema(description = "Flow mode for UI: ADAPTIVE or CAT", example = "ADAPTIVE")
    private String flowMode;

    @Schema(description = "Adaptive state for confidence-based progress tracking")
    private DiagnosticSessionResponse.AdaptiveState adaptiveState;
}
