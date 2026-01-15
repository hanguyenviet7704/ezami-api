package com.hth.udecareer.eil.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsCardResponse {

    private Long id;
    private String clientId;
    private Long questionId;
    private Long skillId;
    private String certificationCode;

    // SM-2 fields
    private BigDecimal easeFactor;
    private Integer intervalDays;
    private Integer repetitions;

    // Status
    private String status;

    // Review stats
    private Integer totalReviews;
    private Integer correctReviews;
    private Integer lastQuality;
    private Integer streak;

    // Timing
    private LocalDateTime nextReviewAt;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Sync
    private Integer syncVersion;

    // Computed fields
    private Boolean isDue;
    private Long daysUntilReview;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResult {
        private Long cardId;
        private Integer newIntervalDays;
        private BigDecimal newEaseFactor;
        private Integer newRepetitions;
        private String newStatus;
        private LocalDateTime nextReviewAt;
        private Integer streak;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SrsStats {
        private Long totalCards;
        private Long dueCards;
        private Long newCards;
        private Long learningCards;
        private Long reviewCards;
        private Long suspendedCards;
        private Long totalReviews;
        private Long correctReviews;
        private BigDecimal accuracy;
        private BigDecimal averageEaseFactor;
        private Map<String, Long> cardsByStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResponse {
        private List<SrsCardResponse> serverCards;
        private List<SrsCardResponse> conflictCards;
        private Long syncTimestamp;
        private Integer cardsCreated;
        private Integer cardsUpdated;
        private Integer cardsConflicted;
    }
}
