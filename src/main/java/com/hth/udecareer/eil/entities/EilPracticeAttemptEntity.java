package com.hth.udecareer.eil.entities;

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
@Table(name = "eil_practice_attempts")
public class EilPracticeAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private EilPracticeSessionEntity session;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private EilSkillEntity skill;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(name = "question_difficulty")
    private Integer questionDifficulty;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "mastery_before", precision = 5, scale = 4)
    private BigDecimal masteryBefore;

    @Column(name = "mastery_after", precision = 5, scale = 4)
    private BigDecimal masteryAfter;

    @Column(name = "mastery_delta", precision = 5, scale = 4)
    private BigDecimal masteryDelta;

    @Column(name = "explanation_id")
    private Long explanationId;

    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }

    public double getMasteryDeltaAsDouble() {
        return masteryDelta != null ? masteryDelta.doubleValue() : 0.0;
    }
}
