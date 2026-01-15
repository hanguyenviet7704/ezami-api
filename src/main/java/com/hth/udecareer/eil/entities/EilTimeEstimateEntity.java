package com.hth.udecareer.eil.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for Time-to-Certification estimates.
 * Tracks user progress and estimates time needed to achieve certification readiness.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eil_time_estimates", indexes = {
    @Index(name = "idx_estimate_user_id", columnList = "user_id"),
    @Index(name = "idx_estimate_cert_code", columnList = "certification_code"),
    @Index(name = "idx_estimate_user_cert", columnList = "user_id, certification_code")
})
public class EilTimeEstimateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "certification_code", nullable = false, length = 30)
    private String certificationCode;

    @Column(name = "certification_name", length = 100)
    private String certificationName;

    // Current state
    @Column(name = "current_mastery", precision = 5, scale = 4)
    private BigDecimal currentMastery;

    @Column(name = "target_mastery", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal targetMastery = new BigDecimal("0.8000"); // 80% default

    @Column(name = "mastery_gap", precision = 5, scale = 4)
    private BigDecimal masteryGap;

    // Time estimates
    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "estimated_sessions")
    private Integer estimatedSessions;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "estimated_ready_date")
    private LocalDate estimatedReadyDate;

    // Progress tracking
    @Column(name = "days_practiced")
    @Builder.Default
    private Integer daysPracticed = 0;

    @Column(name = "total_study_hours", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal totalStudyHours = BigDecimal.ZERO;

    @Column(name = "questions_practiced")
    @Builder.Default
    private Integer questionsPracticed = 0;

    @Column(name = "sessions_completed")
    @Builder.Default
    private Integer sessionsCompleted = 0;

    // Learning pace
    @Column(name = "avg_daily_hours", precision = 4, scale = 2)
    private BigDecimal avgDailyHours;

    @Column(name = "avg_sessions_per_day", precision = 4, scale = 2)
    private BigDecimal avgSessionsPerDay;

    @Column(name = "mastery_velocity", precision = 6, scale = 5)
    private BigDecimal masteryVelocity; // Mastery gain per day

    // Confidence
    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "confidence_level", length = 20)
    private String confidenceLevel; // LOW, MEDIUM, HIGH

    // Status
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, PAUSED, COMPLETED, ABANDONED

    // Historical data (JSON)
    @Column(name = "progress_history", columnDefinition = "json")
    private String progressHistory; // Array of {date, mastery, hoursStudied}

    @Column(name = "milestones", columnDefinition = "json")
    private String milestones; // Array of achieved milestones

    // Recommendations
    @Column(name = "recommended_daily_hours", precision = 4, scale = 2)
    private BigDecimal recommendedDailyHours;

    @Column(name = "focus_areas", columnDefinition = "json")
    private String focusAreas; // Skills to focus on

    // Timing
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "start_date")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "target_date")
    private LocalDate targetDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
