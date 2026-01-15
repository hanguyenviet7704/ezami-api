package com.hth.udecareer.eil.entities;

import com.hth.udecareer.config.TimezoneConfig;
import com.hth.udecareer.eil.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eil_diagnostic_attempts")
public class EilDiagnosticAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @Column(name = "test_type", length = 30)
    @Builder.Default
    private String testType = "TOEIC";

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = SessionStatus.IN_PROGRESS.getCode();

    @Column(name = "total_questions")
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "answered_questions")
    @Builder.Default
    private Integer answeredQuestions = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "raw_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rawScore = BigDecimal.ZERO;

    @Column(name = "estimated_level", length = 20)
    private String estimatedLevel;

    @Column(name = "estimated_score_min")
    private Integer estimatedScoreMin;

    @Column(name = "estimated_score_max")
    private Integer estimatedScoreMax;

    @Column(name = "skill_coverage", columnDefinition = "JSON")
    private String skillCoverage;

    @Column(name = "weak_skills", columnDefinition = "JSON")
    private String weakSkills;

    @Column(name = "strong_skills", columnDefinition = "JSON")
    private String strongSkills;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimezoneConfig.getCurrentVietnamTime();
        updatedAt = TimezoneConfig.getCurrentVietnamTime();
        if (startTime == null) {
            startTime = TimezoneConfig.getCurrentVietnamTime();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = TimezoneConfig.getCurrentVietnamTime();
    }

    public SessionStatus getStatusEnum() {
        return SessionStatus.fromCode(status);
    }

    public void setStatusEnum(SessionStatus statusEnum) {
        this.status = statusEnum.getCode();
    }
}
