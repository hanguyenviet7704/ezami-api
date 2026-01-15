package com.hth.udecareer.eil.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after submitting a practice answer")
public class PracticeResultResponse {

    @Schema(description = "Attempt ID", example = "12345")
    private Long attemptId;

    @Schema(description = "Whether the answer was correct", example = "true")
    private Boolean isCorrect;

    @Schema(description = "Correct answer", example = "[false, true, false, false]")
    private List<Boolean> correctAnswer;

    @Schema(description = "Points earned for this question", example = "10")
    private Integer pointsEarned;

    // Mastery update info
    @Schema(description = "Mastery level before this answer", example = "0.45")
    private Double masteryBefore;

    @Schema(description = "Mastery level after this answer", example = "0.52")
    private Double masteryAfter;

    @Schema(description = "Change in mastery", example = "0.07")
    private Double masteryDelta;

    @Schema(description = "Mastery label after update", example = "PROFICIENT")
    private String masteryLabel;

    // Explanation (if requested)
    @Schema(description = "Explanation (if requested)")
    private ExplanationResponse explanation;

    // Session stats
    @Schema(description = "Questions answered in session", example = "5")
    private Integer questionsAnswered;

    @Schema(description = "Correct answers in session", example = "4")
    private Integer correctCount;

    @Schema(description = "Session accuracy (0.0 - 1.0)", example = "0.8")
    private Double sessionAccuracy;

    // Next question (null if session complete)
    @Schema(description = "Next question (null if session complete)")
    private NextQuestionResponse nextQuestion;
}
