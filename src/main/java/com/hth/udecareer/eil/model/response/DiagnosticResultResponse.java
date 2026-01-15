package com.hth.udecareer.eil.model.response;

import com.hth.udecareer.eil.model.dto.WeakSkillDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after completing a diagnostic test")
public class DiagnosticResultResponse {

    @Schema(description = "Session ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Session status", example = "COMPLETED")
    private String status;

    @Schema(description = "Total questions answered", example = "30")
    private Integer totalQuestions;

    @Schema(description = "Number of correct answers", example = "18")
    private Integer correctCount;

    @Schema(description = "Raw score percentage (0-100)", example = "60.0")
    private Double rawScore;

    @Schema(description = "Estimated proficiency level", example = "INTERMEDIATE")
    private String estimatedLevel;

    @Schema(description = "Estimated minimum score", example = "450")
    private Integer estimatedScoreMin;

    @Schema(description = "Estimated maximum score", example = "550")
    private Integer estimatedScoreMax;

    @Schema(description = "Skill mastery results")
    private List<SkillMasteryResponse> skillResults;

    @Schema(description = "Category-level scores breakdown")
    private Map<String, CategoryScore> categoryScores;

    @Schema(description = "Top 5 weakest skills")
    private List<WeakSkillDto> weakSkills;

    @Schema(description = "AI-generated recommendations")
    private List<String> recommendations;

    @Schema(description = "When diagnostic was completed")
    private LocalDateTime completedAt;

    @Schema(description = "Total time spent in seconds", example = "1800")
    private Integer timeSpentSeconds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Category-level score breakdown")
    public static class CategoryScore {
        @Schema(description = "Category name", example = "LISTENING")
        private String category;

        @Schema(description = "Total questions in this category", example = "10")
        private Integer totalQuestions;

        @Schema(description = "Correct answers in this category", example = "7")
        private Integer correctCount;

        @Schema(description = "Accuracy percentage (0.0-1.0)", example = "0.7")
        private Double accuracy;
    }
}
