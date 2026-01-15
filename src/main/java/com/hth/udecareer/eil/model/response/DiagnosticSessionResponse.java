package com.hth.udecareer.eil.model.response;

import com.hth.udecareer.model.response.QuestionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after starting a diagnostic test")
public class DiagnosticSessionResponse {

    @Schema(description = "Unique session ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Session status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Total questions in diagnostic", example = "30")
    private Integer totalQuestions;

    @Schema(description = "Current question number (1-based)", example = "1")
    private Integer currentQuestion;

    @Schema(description = "First question to answer (adaptive mode - call /next-question for subsequent questions)")
    private QuestionResponse firstQuestion;

    @Schema(description = "All questions for this diagnostic session (DEPRECATED - now using adaptive mode, use /next-question endpoint)")
    @Deprecated
    private List<QuestionResponse> questions;

    @Schema(description = "Session start time")
    private LocalDateTime startTime;

    @Schema(description = "Timeout in minutes", example = "60")
    private Integer timeoutMinutes;

    @Schema(description = "Number of questions already answered", example = "5")
    private Integer answeredQuestions;

    @Schema(description = "Test type", example = "TOEIC")
    private String testType;

    @Schema(description = "Assessment mode: CAREER_ASSESSMENT (random skills to determine level) or CERTIFICATION_PRACTICE (specific cert)")
    private String mode;

    @Schema(description = "Certification code if mode is CERTIFICATION_PRACTICE", example = "PSM_I")
    private String certificationCode;

    @Schema(description = "Flow mode for UI: ADAPTIVE (adaptive diagnostic) or CAT (computer adaptive test)", example = "ADAPTIVE")
    private String flowMode;

    @Schema(description = "Adaptive state for confidence-based progress tracking")
    private AdaptiveState adaptiveState;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdaptiveState {
        @Schema(description = "Current confidence level (0.0 - 1.0)", example = "0.75")
        private Double currentConfidence;

        @Schema(description = "Target confidence to finish early (0.0 - 1.0)", example = "0.80")
        private Double targetConfidence;

        @Schema(description = "Maximum questions if confidence not reached", example = "15")
        private Integer maxQuestions;

        @Schema(description = "Whether session can terminate early based on confidence", example = "true")
        private Boolean canTerminateEarly;
    }
}
