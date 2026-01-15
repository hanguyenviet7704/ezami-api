package com.hth.udecareer.eil.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEstimateResponse {

    private Long id;
    private String certificationCode;
    private String certificationName;

    // Current state
    private BigDecimal currentMastery;
    private BigDecimal targetMastery;
    private BigDecimal masteryGap;

    // Estimates
    private Integer estimatedDays;
    private Integer estimatedHours;
    private Integer estimatedSessions;
    private LocalDate estimatedReadyDate;

    // Progress
    private Integer daysPracticed;
    private BigDecimal totalStudyHours;
    private Integer questionsPracticed;
    private Integer sessionsCompleted;

    // Pace
    private BigDecimal avgDailyHours;
    private BigDecimal avgSessionsPerDay;
    private BigDecimal masteryVelocity;

    // Confidence
    private BigDecimal confidence;
    private String confidenceLevel;

    // Status
    private String status;

    // Recommendations
    private BigDecimal recommendedDailyHours;
    private List<String> focusAreas;

    // Dates
    private LocalDate startDate;
    private LocalDate targetDate;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Progress percentage
    private BigDecimal progressPercentage;
    private Boolean onTrack;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressSnapshot {
        private LocalDate date;
        private BigDecimal mastery;
        private BigDecimal hoursStudied;
        private Integer questionsAnswered;
        private BigDecimal accuracy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaceAnalysis {
        private BigDecimal currentPace;
        private BigDecimal requiredPace;
        private BigDecimal paceDifference;
        private Boolean needsAcceleration;
        private BigDecimal suggestedDailyHours;
        private Integer daysRemaining;
        private LocalDate projectedCompletionDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Milestone {
        private String name;
        private BigDecimal targetMastery;
        private LocalDate achievedDate;
        private Boolean achieved;
    }
}
