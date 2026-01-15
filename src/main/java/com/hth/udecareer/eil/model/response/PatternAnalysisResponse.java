package com.hth.udecareer.eil.model.response;

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
public class PatternAnalysisResponse {

    private Long id;
    private String sessionId;
    private String sessionType;
    private String certificationCode;

    // Session metrics
    private Integer totalQuestions;
    private Integer correctCount;
    private BigDecimal accuracy;
    private Integer avgTimePerQuestion;
    private Integer totalTimeSeconds;

    // Time patterns
    private Integer sessionHour;
    private String timeOfDay;
    private Integer dayOfWeek;

    // Detected patterns
    private List<DetectedPattern> detectedPatterns;

    // Fatigue analysis
    private Boolean fatigueDetected;
    private BigDecimal fatigueScore;

    // Speed vs Accuracy
    private String speedAccuracyTradeoff;

    // Category analysis
    private List<CategoryAnalysis> weakCategories;
    private List<CategoryAnalysis> strongCategories;

    // Recommendations
    private List<String> recommendations;

    // Confidence
    private BigDecimal confidence;

    private LocalDateTime sessionStartTime;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectedPattern {
        private String type;
        private String description;
        private BigDecimal confidence;
        private Map<String, Object> details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryAnalysis {
        private String category;
        private Integer totalQuestions;
        private Integer correctCount;
        private BigDecimal accuracy;
        private BigDecimal avgTimeSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternStats {
        private Long totalSessions;
        private Long totalQuestions;
        private BigDecimal overallAccuracy;
        private Map<String, BigDecimal> accuracyByTimeOfDay;
        private String bestTimeOfDay;
        private Long fatigueSessions;
        private BigDecimal avgFatigueScore;
    }
}
