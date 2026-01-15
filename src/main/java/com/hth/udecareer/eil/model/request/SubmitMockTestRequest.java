package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to submit a mock test and save analysis result")
public class SubmitMockTestRequest {

    @NotNull(message = "Quiz ID is required")
    @Schema(description = "Quiz ID of the mock test", example = "456", required = true)
    private Long quizId;

    @Schema(description = "Activity ID from quiz submission (if already submitted via regular quiz API)", example = "789")
    private Long activityId;

    @NotNull(message = "Test type is required")
    @Schema(description = "Test type", example = "TOEIC", required = true)
    private String testType;

    @NotNull(message = "Start time is required")
    @Schema(description = "Start time in epoch seconds", example = "1704067200", required = true)
    private Long startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "End time in epoch seconds", example = "1704074400", required = true)
    private Long endTime;

    @NotEmpty(message = "Answer data cannot be empty")
    @Schema(description = "List of question answers", required = true)
    private List<AnswerData> answers;

    @Schema(description = "Optional session ID to link with diagnostic/practice session")
    private String sessionId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Answer data for a single question")
    public static class AnswerData {

        @NotNull(message = "Question ID is required")
        @Schema(description = "Question ID", example = "123", required = true)
        private Long questionId;

        @NotNull(message = "Answer data is required")
        @Schema(description = "Answer choices as boolean array", example = "[true, false, false, false]", required = true)
        private List<Boolean> answerData;

        @Schema(description = "Time spent on this question in seconds", example = "45")
        private Integer timeSpentSeconds;
    }
}
