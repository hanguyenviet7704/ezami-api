package com.hth.udecareer.eil.model.response;

import com.hth.udecareer.eil.enums.EstimatedLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's skill map overview")
public class SkillMapResponse {

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "Test type", example = "TOEIC")
    private String testType;

    @Schema(description = "Overall mastery level (0.0-1.0)", example = "0.65")
    private BigDecimal overallMastery;

    @Schema(description = "Estimated proficiency level", example = "INTERMEDIATE")
    private EstimatedLevel estimatedLevel;

    @Schema(description = "Estimated score range")
    private ScoreRange estimatedScore;

    @Schema(description = "Category-level mastery breakdown")
    private CategoryMastery categoryMastery;

    @Schema(description = "List of all skill masteries")
    private List<SkillMasteryResponse> skills;

    @Schema(description = "Top weak skills to focus on")
    private List<SkillMasteryResponse> weakSkills;

    @Schema(description = "Top strong skills")
    private List<SkillMasteryResponse> strongSkills;

    @Schema(description = "Whether user has completed diagnostic test")
    private Boolean diagnosticCompleted;

    @Schema(description = "Last diagnostic completion time")
    private LocalDateTime lastDiagnosticAt;

    @Schema(description = "Total practice sessions completed")
    private Integer totalPracticeSessions;

    @Schema(description = "Total questions answered")
    private Integer totalQuestionsAnswered;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime lastUpdatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Estimated score range")
    public static class ScoreRange {
        @Schema(description = "Minimum estimated score", example = "550")
        private Integer min;

        @Schema(description = "Maximum estimated score", example = "650")
        private Integer max;

        @Schema(description = "Mid-point score", example = "600")
        private Integer mid;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Category-level mastery breakdown")
    public static class CategoryMastery {
        @Schema(description = "Listening category mastery (0.0-1.0)", example = "0.70")
        private BigDecimal listening;

        @Schema(description = "Reading category mastery (0.0-1.0)", example = "0.60")
        private BigDecimal reading;

        @Schema(description = "Grammar mastery (0.0-1.0)", example = "0.55")
        private BigDecimal grammar;

        @Schema(description = "Vocabulary mastery (0.0-1.0)", example = "0.65")
        private BigDecimal vocabulary;

        @Schema(description = "Detailed part scores")
        private Map<String, BigDecimal> partScores;
    }
}
