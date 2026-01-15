package com.hth.udecareer.eil.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity mapping to WordPress wp_ez_question_skills table.
 * This table stores question-to-skill mappings from ezami-admin-tools plugin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wp_ez_question_skills")
public class WpEzQuestionSkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private WpEzSkillEntity skill;

    @Column(name = "weight", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "confidence", length = 10)
    @Builder.Default
    private String confidence = "unmapped";

    @Column(name = "mapped_by")
    private Long mappedBy;

    @Column(name = "mapped_at")
    private LocalDateTime mappedAt;

    // Helper methods
    public boolean isHighConfidence() {
        return "high".equals(confidence);
    }

    public boolean isMediumConfidence() {
        return "medium".equals(confidence);
    }

    public boolean isLowConfidence() {
        return "low".equals(confidence);
    }

    public boolean isUnmapped() {
        return "unmapped".equals(confidence);
    }
}
