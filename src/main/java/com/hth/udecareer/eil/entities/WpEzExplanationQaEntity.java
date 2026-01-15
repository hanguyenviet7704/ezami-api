package com.hth.udecareer.eil.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity mapping to WordPress wp_ez_explanation_qa table.
 * This table stores AI-generated explanations for quiz questions from ezami-admin-tools plugin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wp_ez_explanation_qa")
public class WpEzExplanationQaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "answers_json", columnDefinition = "JSON", nullable = false)
    private String answersJson;

    @Column(name = "explanation_json", columnDefinition = "JSON")
    private String explanationJson;

    @Column(name = "explanation_text", columnDefinition = "TEXT")
    private String explanationText;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "rating", length = 10)
    private String rating; // 'good', 'bad', 'neutral'

    @Column(name = "flags", columnDefinition = "JSON")
    private String flags;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Helper methods
    public boolean isReviewed() {
        return reviewedAt != null;
    }

    public boolean isRatedGood() {
        return "good".equals(rating);
    }

    public boolean isRatedBad() {
        return "bad".equals(rating);
    }
}
