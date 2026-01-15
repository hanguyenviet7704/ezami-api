package com.hth.udecareer.eil.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's test readiness assessment")
public class ReadinessResponse {

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "Test type", example = "TOEIC")
    private String testType;

    @Schema(description = "User's target score", example = "700")
    private Integer targetScore;

    // Readiness Metrics
    @Schema(description = "Overall readiness (0.0-1.0)", example = "0.72")
    private BigDecimal overallReadiness;

    @Schema(description = "Readiness status", example = "ALMOST_READY")
    private ReadinessStatus readinessStatus;

    @Schema(description = "Listening section readiness (0.0-1.0)", example = "0.75")
    private BigDecimal listeningReadiness;

    @Schema(description = "Reading section readiness (0.0-1.0)", example = "0.68")
    private BigDecimal readingReadiness;

    @Schema(description = "Category-wise readiness breakdown")
    private Map<String, BigDecimal> categoryReadiness;

    // Prediction
    @Schema(description = "Current predicted score", example = "650")
    private Integer predictedScore;

    @Schema(description = "Pass probability (0.0-1.0)", example = "0.65")
    private BigDecimal passProbability;

    @Schema(description = "Score gap to target", example = "50")
    private Integer gapToTarget;

    // Progress
    @Schema(description = "Days of active practice", example = "14")
    private Integer daysPracticed;

    @Schema(description = "Total questions answered", example = "450")
    private Integer questionsAnswered;

    @Schema(description = "Practice sessions completed", example = "25")
    private Integer sessionsCompleted;

    @Schema(description = "Mock tests completed", example = "2")
    private Integer mocksCompleted;

    @Schema(description = "Current streak (consecutive days)", example = "5")
    private Integer currentStreak;

    // Recommendations
    @Schema(description = "Priority skills to work on")
    private List<PrioritySkillInfo> prioritySkills;

    @Schema(description = "Estimated days until ready", example = "21")
    private Integer estimatedDaysToReady;

    @Schema(description = "Recommended daily practice minutes", example = "45")
    private Integer recommendedDailyMinutes;

    @Schema(description = "Next recommended action")
    private NextAction nextAction;

    // History
    @Schema(description = "Readiness trend (last 7 snapshots)")
    private List<ReadinessTrend> trend;

    @Schema(description = "Assessment date")
    private LocalDate assessmentDate;

    @Schema(description = "Last activity timestamp")
    private LocalDateTime lastActivityAt;

    public enum ReadinessStatus {
        @Schema(description = "Not ready - significant preparation needed")
        NOT_READY,

        @Schema(description = "Getting started - early stage preparation")
        GETTING_STARTED,

        @Schema(description = "Making progress - on track")
        MAKING_PROGRESS,

        @Schema(description = "Almost ready - minor improvements needed")
        ALMOST_READY,

        @Schema(description = "Ready - likely to pass")
        READY,

        @Schema(description = "Well prepared - high confidence")
        WELL_PREPARED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Priority skill information")
    public static class PrioritySkillInfo {
        @Schema(description = "Skill ID", example = "15")
        private Long skillId;

        @Schema(description = "Skill code", example = "RC_P5_GRAMMAR_TENSE")
        private String skillCode;

        @Schema(description = "Skill name", example = "Grammar - Verb Tenses")
        private String skillName;

        @Schema(description = "Current mastery (0.0-1.0)", example = "0.40")
        private BigDecimal currentMastery;

        @Schema(description = "Readiness for this skill (0.0-1.0)", example = "0.45")
        private BigDecimal readiness;

        @Schema(description = "Improvement potential (how much this affects overall score)", example = "HIGH")
        private String impactLevel;

        @Schema(description = "Recommended practice questions", example = "30")
        private Integer recommendedQuestions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Next recommended action for user")
    public static class NextAction {
        @Schema(description = "Action type", example = "PRACTICE_SKILL")
        private ActionType type;

        @Schema(description = "Action description", example = "Practice Grammar - Verb Tenses")
        private String description;

        @Schema(description = "Target skill ID (if applicable)", example = "15")
        private Long skillId;

        @Schema(description = "Estimated duration in minutes", example = "20")
        private Integer estimatedMinutes;
    }

    public enum ActionType {
        @Schema(description = "Take diagnostic test")
        TAKE_DIAGNOSTIC,

        @Schema(description = "Practice specific skill")
        PRACTICE_SKILL,

        @Schema(description = "Review weak areas")
        REVIEW_WEAK_AREAS,

        @Schema(description = "Take mock test")
        TAKE_MOCK_TEST,

        @Schema(description = "Continue mixed practice")
        MIXED_PRACTICE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Readiness trend point")
    public static class ReadinessTrend {
        @Schema(description = "Snapshot date")
        private LocalDate date;

        @Schema(description = "Overall readiness on this date", example = "0.68")
        private BigDecimal readiness;

        @Schema(description = "Predicted score on this date", example = "620")
        private Integer predictedScore;

        @Schema(description = "Questions answered that day", example = "35")
        private Integer questionsAnswered;
    }
}
