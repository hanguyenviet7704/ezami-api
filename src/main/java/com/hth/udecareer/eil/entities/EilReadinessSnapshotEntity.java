package com.hth.udecareer.eil.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eil_readiness_snapshots")
public class EilReadinessSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "test_type", length = 30)
    private String testType;

    @Column(name = "target_score")
    private Integer targetScore;

    @Column(name = "overall_readiness", precision = 5, scale = 4)
    private BigDecimal overallReadiness;

    @Column(name = "listening_readiness", precision = 5, scale = 4)
    private BigDecimal listeningReadiness;

    @Column(name = "reading_readiness", precision = 5, scale = 4)
    private BigDecimal readingReadiness;

    @Column(name = "category_readiness", columnDefinition = "json")
    private String categoryReadiness;

    @Column(name = "predicted_score")
    private Integer predictedScore;

    @Column(name = "pass_probability", precision = 5, scale = 4)
    private BigDecimal passProbability;

    @Column(name = "gap_to_target")
    private Integer gapToTarget;

    @Column(name = "days_practiced")
    private Integer daysPracticed;

    @Column(name = "questions_answered")
    private Integer questionsAnswered;

    @Column(name = "sessions_completed")
    private Integer sessionsCompleted;

    @Column(name = "mocks_completed")
    private Integer mocksCompleted;

    @Column(name = "priority_skills", columnDefinition = "json")
    private String prioritySkills;

    @Column(name = "estimated_days_to_ready")
    private Integer estimatedDaysToReady;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

