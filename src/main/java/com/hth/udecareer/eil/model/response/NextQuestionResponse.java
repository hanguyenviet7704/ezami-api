package com.hth.udecareer.eil.model.response;

import com.hth.udecareer.eil.model.dto.SkillDto;
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
@Schema(description = "Next question in practice session")
public class NextQuestionResponse {

    @Schema(description = "Session ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Question number in session (1-based)", example = "5")
    private Integer questionNumber;

    @Schema(description = "Total questions in session", example = "20")
    private Integer totalQuestions;

    @Schema(description = "Question data")
    private QuestionResponse question;

    @Schema(description = "Target skill for this question")
    private SkillDto targetSkill;

    @Schema(description = "Question difficulty (1-5)", example = "3")
    private Integer difficulty;

    @Schema(description = "Whether this is the last question", example = "false")
    private Boolean isLastQuestion;
}
