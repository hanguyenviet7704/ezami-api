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
 * Entity for Spaced Repetition System (SRS) cards.
 * Implements SM-2 algorithm for optimal review scheduling.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eil_srs_cards", indexes = {
    @Index(name = "idx_srs_user_id", columnList = "user_id"),
    @Index(name = "idx_srs_question_id", columnList = "question_id"),
    @Index(name = "idx_srs_skill_id", columnList = "skill_id"),
    @Index(name = "idx_srs_next_review", columnList = "next_review_at"),
    @Index(name = "idx_srs_user_due", columnList = "user_id, next_review_at")
})
public class EilSrsCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "certification_code", length = 30)
    private String certificationCode;

    // SM-2 Algorithm fields
    @Column(name = "ease_factor", precision = 5, scale = 3)
    @Builder.Default
    private BigDecimal easeFactor = new BigDecimal("2.500"); // Default EF = 2.5

    @Column(name = "interval_days")
    @Builder.Default
    private Integer intervalDays = 1; // Days until next review

    @Column(name = "repetitions")
    @Builder.Default
    private Integer repetitions = 0; // Number of successful reviews

    @Column(name = "quality_history", columnDefinition = "json")
    private String qualityHistory; // JSON array of past quality scores

    // Card status
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "NEW"; // NEW, LEARNING, REVIEW, SUSPENDED

    // Review tracking
    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "correct_reviews")
    @Builder.Default
    private Integer correctReviews = 0;

    @Column(name = "last_quality")
    private Integer lastQuality; // 0-5 scale (SM-2)

    @Column(name = "streak")
    @Builder.Default
    private Integer streak = 0;

    // Timing
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Sync tracking for offline support
    @Column(name = "client_id", length = 50)
    private String clientId; // Client-generated ID for sync

    @Column(name = "sync_version")
    @Builder.Default
    private Integer syncVersion = 1;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (nextReviewAt == null) {
            nextReviewAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        syncVersion = (syncVersion != null ? syncVersion : 0) + 1;
    }
}
