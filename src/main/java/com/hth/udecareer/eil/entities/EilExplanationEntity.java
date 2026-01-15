package com.hth.udecareer.eil.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eil_explanations")
public class EilExplanationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cache_key", nullable = false, unique = true, length = 64)
    private String cacheKey;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "vi";

    @Column(name = "explanation_json", nullable = false, columnDefinition = "JSON")
    private String explanationJson;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "why_correct", columnDefinition = "TEXT")
    private String whyCorrect;

    @Column(name = "why_wrong", columnDefinition = "TEXT")
    private String whyWrong;

    @Column(name = "key_points", columnDefinition = "JSON")
    private String keyPoints;

    @Column(name = "grammar_rule", columnDefinition = "TEXT")
    private String grammarRule;

    @Column(name = "vocabulary_tip", columnDefinition = "TEXT")
    private String vocabularyTip;

    @Column(name = "examples", columnDefinition = "JSON")
    private String examples;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "prompt_version")
    @Builder.Default
    private Integer promptVersion = 1;

    @Column(name = "tokens_used")
    @Builder.Default
    private Integer tokensUsed = 0;

    @Column(name = "generation_time_ms")
    @Builder.Default
    private Integer generationTimeMs = 0;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "hit_count")
    @Builder.Default
    private Integer hitCount = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void incrementHitCount() {
        this.hitCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
}
