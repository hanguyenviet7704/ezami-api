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
@Table(name = "eil_skill_mastery",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
public class EilSkillMasteryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private EilSkillEntity skill;

    @Column(name = "mastery_level", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal masteryLevel = new BigDecimal("0.5000");

    @Column(name = "confidence", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal confidence = new BigDecimal("0.1000");

    @Column(name = "attempts")
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "correct_count")
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "streak")
    @Builder.Default
    private Integer streak = 0;

    @Column(name = "last_practiced_at")
    private LocalDateTime lastPracticedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public double getMasteryLevelAsDouble() {
        return masteryLevel != null ? masteryLevel.doubleValue() : 0.5;
    }

    public void setMasteryLevelFromDouble(double level) {
        this.masteryLevel = BigDecimal.valueOf(level);
    }
}
