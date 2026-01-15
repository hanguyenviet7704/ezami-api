package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to analyze a learning session for patterns")
public class PatternAnalysisRequest {

    @Schema(description = "Session ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Session type", example = "PRACTICE")
    private String sessionType;

    @Schema(description = "Certification code", example = "PSM_I")
    private String certificationCode;

    @Schema(description = "Total questions in session")
    private Integer totalQuestions;

    @Schema(description = "Number of correct answers")
    private Integer correctCount;

    @Schema(description = "Total time spent in seconds")
    private Integer totalTimeSeconds;

    @Schema(description = "Session start time")
    private LocalDateTime sessionStartTime;

    @Schema(description = "List of question results for detailed analysis")
    private List<QuestionResult> questionResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionResult {
        private Long questionId;
        private Long skillId;
        private String category;
        private Boolean isCorrect;
        private Integer timeSpentSeconds;
        private Integer questionOrder;
    }
}
