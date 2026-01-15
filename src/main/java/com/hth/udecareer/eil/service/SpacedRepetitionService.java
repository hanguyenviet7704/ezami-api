package com.hth.udecareer.eil.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.eil.entities.EilSrsCardEntity;
import com.hth.udecareer.eil.model.request.SrsCardRequest;
import com.hth.udecareer.eil.model.response.SrsCardResponse;
import com.hth.udecareer.eil.repository.EilSrsCardRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Spaced Repetition System (SRS) using SM-2 algorithm.
 * Provides optimal review scheduling for knowledge retention.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private final EilSrsCardRepository srsCardRepository;
    private final ObjectMapper objectMapper;

    // SM-2 algorithm constants
    private static final BigDecimal MIN_EASE_FACTOR = new BigDecimal("1.300");
    private static final BigDecimal DEFAULT_EASE_FACTOR = new BigDecimal("2.500");
    private static final int INITIAL_INTERVAL = 1;
    private static final int SECOND_INTERVAL = 6;

    /**
     * Create a new SRS card.
     */
    @Transactional
    public SrsCardResponse createCard(Long userId, SrsCardRequest request) {
        // Check if card already exists
        Optional<EilSrsCardEntity> existing = srsCardRepository
                .findByUserIdAndQuestionId(userId, request.getQuestionId());

        if (existing.isPresent()) {
            log.info("SRS card already exists for user {} question {}", userId, request.getQuestionId());
            return entityToResponse(existing.get());
        }

        EilSrsCardEntity card = EilSrsCardEntity.builder()
                .userId(userId)
                .clientId(request.getClientId())
                .questionId(request.getQuestionId())
                .skillId(request.getSkillId())
                .certificationCode(request.getCertificationCode())
                .easeFactor(DEFAULT_EASE_FACTOR)
                .intervalDays(INITIAL_INTERVAL)
                .repetitions(0)
                .status("NEW")
                .totalReviews(0)
                .correctReviews(0)
                .streak(0)
                .nextReviewAt(LocalDateTime.now())
                .syncVersion(1)
                .build();

        card = srsCardRepository.save(card);
        log.info("Created SRS card {} for user {} question {}", card.getId(), userId, request.getQuestionId());
        return entityToResponse(card);
    }

    /**
     * Bulk create SRS cards.
     */
    @Transactional
    public List<SrsCardResponse> bulkCreateCards(Long userId, SrsCardRequest.BulkCreateRequest request) {
        if (request.getCards() == null || request.getCards().isEmpty()) {
            return new ArrayList<>();
        }

        List<SrsCardResponse> responses = new ArrayList<>();
        for (SrsCardRequest cardRequest : request.getCards()) {
            try {
                responses.add(createCard(userId, cardRequest));
            } catch (Exception e) {
                log.warn("Failed to create card for question {}: {}", cardRequest.getQuestionId(), e.getMessage());
            }
        }
        return responses;
    }

    /**
     * Get cards due for review.
     */
    public Page<SrsCardResponse> getDueCards(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EilSrsCardEntity> dueCards = srsCardRepository.findDueCards(userId, LocalDateTime.now(), pageable);
        return dueCards.map(this::entityToResponse);
    }

    /**
     * Get all cards for a user.
     */
    public Page<SrsCardResponse> getCards(Long userId, String status, String certificationCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EilSrsCardEntity> cards;
        boolean hasStatus = StringUtils.hasText(status);
        boolean hasCertification = StringUtils.hasText(certificationCode);

        if (hasStatus && hasCertification) {
            cards = srsCardRepository.findByUserIdAndStatusAndCertificationCode(
                    userId, status, certificationCode, pageable);
        } else if (hasStatus) {
            cards = srsCardRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (hasCertification) {
            cards = srsCardRepository.findByUserIdAndCertificationCode(userId, certificationCode, pageable);
        } else {
            cards = srsCardRepository.findByUserId(userId, pageable);
        }
        return cards.map(this::entityToResponse);
    }

    /**
     * Get a specific card.
     */
    public SrsCardResponse getCard(Long userId, Long cardId) {
        EilSrsCardEntity card = srsCardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!card.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return entityToResponse(card);
    }

    /**
     * Record a review using SM-2 algorithm.
     */
    @Transactional
    public SrsCardResponse.ReviewResult recordReview(Long userId, Long cardId, SrsCardRequest.ReviewRequest request) {
        EilSrsCardEntity card = srsCardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!card.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        int quality = request.getQuality();
        log.info("Recording review for card {} with quality {}", cardId, quality);

        // Update review statistics
        card.setTotalReviews(card.getTotalReviews() + 1);
        if (quality >= 3) {
            card.setCorrectReviews(card.getCorrectReviews() + 1);
            card.setStreak(card.getStreak() + 1);
        } else {
            card.setStreak(0);
        }
        card.setLastQuality(quality);
        card.setLastReviewedAt(LocalDateTime.now());

        // Apply SM-2 algorithm
        applySM2Algorithm(card, quality);

        // Update quality history
        updateQualityHistory(card, quality);

        card = srsCardRepository.save(card);
        log.info("Updated SRS card {} - interval: {} days, next review: {}",
                cardId, card.getIntervalDays(), card.getNextReviewAt());

        return SrsCardResponse.ReviewResult.builder()
                .cardId(card.getId())
                .newIntervalDays(card.getIntervalDays())
                .newEaseFactor(card.getEaseFactor())
                .newRepetitions(card.getRepetitions())
                .newStatus(card.getStatus())
                .nextReviewAt(card.getNextReviewAt())
                .streak(card.getStreak())
                .build();
    }

    /**
     * Apply SM-2 algorithm to calculate new interval and ease factor.
     */
    private void applySM2Algorithm(EilSrsCardEntity card, int quality) {
        if (quality < 3) {
            // Incorrect response - reset to learning
            card.setRepetitions(0);
            card.setIntervalDays(INITIAL_INTERVAL);
            card.setStatus("LEARNING");
        } else {
            // Correct response
            if (card.getRepetitions() == 0) {
                card.setIntervalDays(INITIAL_INTERVAL);
            } else if (card.getRepetitions() == 1) {
                card.setIntervalDays(SECOND_INTERVAL);
            } else {
                // I(n) = I(n-1) * EF
                int newInterval = (int) Math.round(card.getIntervalDays() * card.getEaseFactor().doubleValue());
                card.setIntervalDays(newInterval);
            }
            card.setRepetitions(card.getRepetitions() + 1);
            card.setStatus("REVIEW");
        }

        // Update ease factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        double ef = card.getEaseFactor().doubleValue();
        ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        ef = Math.max(ef, MIN_EASE_FACTOR.doubleValue());
        card.setEaseFactor(BigDecimal.valueOf(ef).setScale(3, RoundingMode.HALF_UP));

        // Calculate next review time
        card.setNextReviewAt(LocalDateTime.now().plusDays(card.getIntervalDays()));
    }

    private void updateQualityHistory(EilSrsCardEntity card, int quality) {
        List<Integer> history;
        try {
            if (card.getQualityHistory() != null && !card.getQualityHistory().isEmpty()) {
                history = objectMapper.readValue(card.getQualityHistory(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
            } else {
                history = new ArrayList<>();
            }
            history.add(quality);
            // Keep last 20 entries
            if (history.size() > 20) {
                history = history.subList(history.size() - 20, history.size());
            }
            card.setQualityHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.warn("Failed to update quality history", e);
        }
    }

    /**
     * Update card status (suspend/resume).
     */
    @Transactional
    public SrsCardResponse updateCardStatus(Long userId, Long cardId, String newStatus) {
        EilSrsCardEntity card = srsCardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!card.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        card.setStatus(newStatus);
        card = srsCardRepository.save(card);
        log.info("Updated card {} status to {}", cardId, newStatus);

        return entityToResponse(card);
    }

    /**
     * Delete a card.
     */
    @Transactional
    public void deleteCard(Long userId, Long cardId) {
        EilSrsCardEntity card = srsCardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!card.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        srsCardRepository.delete(card);
        log.info("Deleted SRS card {}", cardId);
    }

    /**
     * Get SRS statistics.
     */
    public SrsCardResponse.SrsStats getStats(Long userId) {
        Long totalCards = srsCardRepository.countByUserId(userId);
        Long dueCards = srsCardRepository.countDueCards(userId, LocalDateTime.now());
        Long totalReviews = srsCardRepository.getTotalReviewsByUserId(userId);
        Long correctReviews = srsCardRepository.getTotalCorrectReviewsByUserId(userId);
        Double avgEf = srsCardRepository.getAverageEaseFactor(userId);

        // Get cards by status
        List<Object[]> statusCounts = srsCardRepository.getCardCountByStatus(userId);
        Map<String, Long> cardsByStatus = new HashMap<>();
        long newCards = 0, learningCards = 0, reviewCards = 0, suspendedCards = 0;

        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            cardsByStatus.put(status, count);

            if ("NEW".equals(status)) newCards = count;
            else if ("LEARNING".equals(status)) learningCards = count;
            else if ("REVIEW".equals(status)) reviewCards = count;
            else if ("SUSPENDED".equals(status)) suspendedCards = count;
        }

        BigDecimal accuracy = BigDecimal.ZERO;
        if (totalReviews != null && totalReviews > 0 && correctReviews != null) {
            accuracy = BigDecimal.valueOf((double) correctReviews / totalReviews)
                    .setScale(4, RoundingMode.HALF_UP);
        }

        return SrsCardResponse.SrsStats.builder()
                .totalCards(totalCards != null ? totalCards : 0L)
                .dueCards(dueCards != null ? dueCards : 0L)
                .newCards(newCards)
                .learningCards(learningCards)
                .reviewCards(reviewCards)
                .suspendedCards(suspendedCards)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .correctReviews(correctReviews != null ? correctReviews : 0L)
                .accuracy(accuracy)
                .averageEaseFactor(avgEf != null ? BigDecimal.valueOf(avgEf).setScale(3, RoundingMode.HALF_UP) : DEFAULT_EASE_FACTOR)
                .cardsByStatus(cardsByStatus)
                .build();
    }

    /**
     * Sync cards from client.
     */
    @Transactional
    public SrsCardResponse.SyncResponse syncCards(Long userId, SrsCardRequest.SyncRequest request) {
        List<SrsCardResponse> serverCards = new ArrayList<>();
        List<SrsCardResponse> conflictCards = new ArrayList<>();
        int created = 0, updated = 0, conflicted = 0;

        if (request.getCards() != null) {
            for (SrsCardRequest.SrsCardSyncData clientCard : request.getCards()) {
                Optional<EilSrsCardEntity> existingOpt = srsCardRepository
                        .findByUserIdAndQuestionId(userId, clientCard.getQuestionId());

                if (existingOpt.isPresent()) {
                    EilSrsCardEntity existing = existingOpt.get();
                    // Check for conflict
                    if (clientCard.getSyncVersion() != null &&
                            existing.getSyncVersion() != null &&
                            clientCard.getSyncVersion() < existing.getSyncVersion()) {
                        conflictCards.add(entityToResponse(existing));
                        conflicted++;
                    } else {
                        // Update from client
                        updateFromSyncData(existing, clientCard);
                        srsCardRepository.save(existing);
                        updated++;
                    }
                } else {
                    // Create new card
                    EilSrsCardEntity newCard = createFromSyncData(userId, clientCard);
                    srsCardRepository.save(newCard);
                    created++;
                }
            }
        }

        // Get cards updated since last sync
        if (request.getLastSyncAt() != null) {
            LocalDateTime since = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(request.getLastSyncAt()),
                    java.time.ZoneId.systemDefault());
            List<EilSrsCardEntity> updatedCards = srsCardRepository.findUpdatedSince(userId, since);
            serverCards = updatedCards.stream().map(this::entityToResponse).collect(Collectors.toList());
        }

        return SrsCardResponse.SyncResponse.builder()
                .serverCards(serverCards)
                .conflictCards(conflictCards)
                .syncTimestamp(System.currentTimeMillis())
                .cardsCreated(created)
                .cardsUpdated(updated)
                .cardsConflicted(conflicted)
                .build();
    }

    private void updateFromSyncData(EilSrsCardEntity entity, SrsCardRequest.SrsCardSyncData data) {
        if (data.getEaseFactor() != null) entity.setEaseFactor(BigDecimal.valueOf(data.getEaseFactor()));
        if (data.getIntervalDays() != null) entity.setIntervalDays(data.getIntervalDays());
        if (data.getRepetitions() != null) entity.setRepetitions(data.getRepetitions());
        if (data.getStatus() != null) entity.setStatus(data.getStatus());
        if (data.getTotalReviews() != null) entity.setTotalReviews(data.getTotalReviews());
        if (data.getCorrectReviews() != null) entity.setCorrectReviews(data.getCorrectReviews());
        if (data.getLastQuality() != null) entity.setLastQuality(data.getLastQuality());
        if (data.getNextReviewAt() != null) {
            entity.setNextReviewAt(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(data.getNextReviewAt()),
                    java.time.ZoneId.systemDefault()));
        }
    }

    private EilSrsCardEntity createFromSyncData(Long userId, SrsCardRequest.SrsCardSyncData data) {
        return EilSrsCardEntity.builder()
                .userId(userId)
                .clientId(data.getClientId())
                .questionId(data.getQuestionId())
                .skillId(data.getSkillId())
                .certificationCode(data.getCertificationCode())
                .easeFactor(data.getEaseFactor() != null ? BigDecimal.valueOf(data.getEaseFactor()) : DEFAULT_EASE_FACTOR)
                .intervalDays(data.getIntervalDays() != null ? data.getIntervalDays() : INITIAL_INTERVAL)
                .repetitions(data.getRepetitions() != null ? data.getRepetitions() : 0)
                .status(data.getStatus() != null ? data.getStatus() : "NEW")
                .totalReviews(data.getTotalReviews() != null ? data.getTotalReviews() : 0)
                .correctReviews(data.getCorrectReviews() != null ? data.getCorrectReviews() : 0)
                .lastQuality(data.getLastQuality())
                .streak(0)
                .nextReviewAt(LocalDateTime.now())
                .syncVersion(data.getSyncVersion() != null ? data.getSyncVersion() : 1)
                .build();
    }

    private SrsCardResponse entityToResponse(EilSrsCardEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        boolean isDue = entity.getNextReviewAt() != null && !entity.getNextReviewAt().isAfter(now);
        long daysUntilReview = entity.getNextReviewAt() != null ?
                Math.max(0, ChronoUnit.DAYS.between(now, entity.getNextReviewAt())) : 0;

        return SrsCardResponse.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
                .questionId(entity.getQuestionId())
                .skillId(entity.getSkillId())
                .certificationCode(entity.getCertificationCode())
                .easeFactor(entity.getEaseFactor())
                .intervalDays(entity.getIntervalDays())
                .repetitions(entity.getRepetitions())
                .status(entity.getStatus())
                .totalReviews(entity.getTotalReviews())
                .correctReviews(entity.getCorrectReviews())
                .lastQuality(entity.getLastQuality())
                .streak(entity.getStreak())
                .nextReviewAt(entity.getNextReviewAt())
                .lastReviewedAt(entity.getLastReviewedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .syncVersion(entity.getSyncVersion())
                .isDue(isDue)
                .daysUntilReview(daysUntilReview)
                .build();
    }
}
