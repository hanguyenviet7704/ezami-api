package com.hth.udecareer.eil.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for storing pattern analysis results from learning sessions.
 * Detects learning patterns like time-of-day performance, fatigue, speed vs accuracy tradeoffs.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eil_pattern_analyses", indexes = {
    @Index(name = "idx_pattern_user_id", columnList = "user_id"),
    @Index(name = "idx_pattern_session_id", columnList = "session_id"),
    @Index(name = "idx_pattern_created_at", columnList = "created_at")
})
public class EilPatternAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", length = 50)
    private String sessionId;

    @Column(name = "session_type", length = 30)
    private String sessionType;

    @Column(name = "certification_code", length = 30)
    private String certificationCode;

    // Session metrics
    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "accuracy", precision = 5, scale = 4)
    private BigDecimal accuracy;

    @Column(name = "avg_time_per_question")
    private Integer avgTimePerQuestion;

    @Column(name = "total_time_seconds")
    private Integer totalTimeSeconds;

    // Time-of-day pattern
    @Column(name = "session_hour")
    private Integer sessionHour;

    @Column(name = "time_of_day", length = 20)
    private String timeOfDay; // MORNING, AFTERNOON, EVENING, NIGHT

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    // Pattern detection results (JSON)
    @Column(name = "detected_patterns", columnDefinition = "json")
    private String detectedPatterns;

    // Fatigue indicator
    @Column(name = "fatigue_detected")
    private Boolean fatigueDetected;

    @Column(name = "fatigue_score", precision = 5, scale = 4)
    private BigDecimal fatigueScore;

    // Speed vs Accuracy analysis
    @Column(name = "speed_accuracy_tradeoff", length = 20)
    private String speedAccuracyTradeoff; // BALANCED, SPEED_FOCUSED, ACCURACY_FOCUSED

    // Weak categories detected (JSON array)
    @Column(name = "weak_categories", columnDefinition = "json")
    private String weakCategories;

    // Strong categories detected (JSON array)
    @Column(name = "strong_categories", columnDefinition = "json")
    private String strongCategories;

    // Recommendations (JSON array)
    @Column(name = "recommendations", columnDefinition = "json")
    private String recommendations;

    // Confidence in analysis
    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "session_start_time")
    private LocalDateTime sessionStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
