package com.hth.udecareer.eil.entities;

import com.hth.udecareer.config.TimezoneConfig;
import com.hth.udecareer.eil.enums.SessionStatus;
import com.hth.udecareer.eil.enums.SessionType;
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
@Table(name = "eil_practice_sessions")
public class EilPracticeSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @Column(name = "session_type", length = 30)
    @Builder.Default
    private String sessionType = SessionType.ADAPTIVE.getCode();

    @Column(name = "target_skill_id")
    private Long targetSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_skill_id", insertable = false, updatable = false)
    private EilSkillEntity targetSkill;

    @Column(name = "target_categories", columnDefinition = "JSON")
    private String targetCategories;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = SessionStatus.ACTIVE.getCode();

    @Column(name = "max_questions")
    @Builder.Default
    private Integer maxQuestions = 20;

    @Column(name = "total_questions")
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "correct_count")
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "current_difficulty", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal currentDifficulty = new BigDecimal("3.00");

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_time_seconds")
    @Builder.Default
    private Integer totalTimeSeconds = 0;

    @Column(name = "mastery_gain", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal masteryGain = BigDecimal.ZERO;

    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;

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

    public SessionType getSessionTypeEnum() {
        return SessionType.fromCode(sessionType);
    }

    public void setSessionTypeEnum(SessionType typeEnum) {
        this.sessionType = typeEnum.getCode();
    }

    public double getAccuracy() {
        if (totalQuestions == 0) return 0.0;
        return (double) correctCount / totalQuestions;
    }
}
