package com.hth.udecareer.eil.model.response;

import com.hth.udecareer.model.response.QuestionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after starting a practice session")
public class PracticeSessionResponse {

    @Schema(description = "Unique session ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Session type", example = "ADAPTIVE")
    private String sessionType;

    @Schema(description = "Session status", example = "ACTIVE")
    private String status;

    @Schema(description = "Maximum questions for session", example = "20")
    private Integer maxQuestions;

    @Schema(description = "Questions served so far", example = "0")
    private Integer questionsServed;

    @Schema(description = "First question to answer")
    private QuestionResponse firstQuestion;

    @Schema(description = "Target skill (for SKILL_FOCUS type)")
    private SkillMasteryResponse targetSkill;

    @Schema(description = "Session start time")
    private LocalDateTime startTime;
}
