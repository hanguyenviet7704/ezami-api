package com.hth.udecareer.eil.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.eil.entities.EilReadinessSnapshotEntity;
import com.hth.udecareer.eil.model.dto.WeakSkillDto;
import com.hth.udecareer.eil.repository.EilReadinessSnapshotRepository;
import com.hth.udecareer.eil.service.MasteryService;
import com.hth.udecareer.eil.service.ReadinessService;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadinessServiceImpl implements ReadinessService {

    private final EilReadinessSnapshotRepository readinessSnapshotRepository;
    private final UserRepository userRepository;
    private final MasteryService masteryService;
    private final ObjectMapper objectMapper;

    @Override
    public EilReadinessSnapshotEntity getMyLatest(Principal principal, String testType) {
        Long userId = getUserIdFromPrincipal(principal);
        if (testType == null || testType.trim().isEmpty()) {
            return readinessSnapshotRepository.findFirstByUserIdOrderBySnapshotDateDesc(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        }
        return readinessSnapshotRepository.findFirstByUserIdAndTestTypeOrderBySnapshotDateDesc(userId, testType)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    @Override
    public PageResponse<EilReadinessSnapshotEntity> getMyHistory(Principal principal, String testType, int page, int size) {
        Long userId = getUserIdFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("snapshotDate").descending());
        Page<EilReadinessSnapshotEntity> p = (testType == null || testType.trim().isEmpty())
                ? readinessSnapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId, pageable)
                : readinessSnapshotRepository.findByUserIdAndTestTypeOrderBySnapshotDateDesc(userId, testType, pageable);
        return PageResponse.of(p);
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        return user.getId();
    }

    @Override
    @Transactional
    public EilReadinessSnapshotEntity createSnapshot(Long userId, String testType, int questionsAnswered, int correctCount) {
        log.info("Creating readiness snapshot for user {} with testType {}", userId, testType);

        // Get overall mastery and category masteries
        BigDecimal overallMastery = masteryService.getOverallMastery(userId);
        Map<String, BigDecimal> categoryMasteries = masteryService.getCategoryMasteries(userId);

        // Get weak skills for priority recommendations
        List<WeakSkillDto> weakSkills = masteryService.getWeakSkills(userId, 5);

        // Calculate predicted score (TOEIC scale: 10-990)
        int predictedScore = calculatePredictedScore(overallMastery);

        // Calculate pass probability based on a target score (default: 600)
        int targetScore = 600;
        BigDecimal passProbability = calculatePassProbability(predictedScore, targetScore);

        // Calculate gap to target
        int gapToTarget = Math.max(0, targetScore - predictedScore);

        // Estimate days to ready (rough estimate based on gap)
        int estimatedDaysToReady = estimateDaysToReady(gapToTarget, overallMastery);

        // Convert maps to JSON
        String categoryReadinessJson = toJson(categoryMasteries);
        String prioritySkillsJson = toJson(weakSkills);

        // Get listening and reading specific readiness
        BigDecimal listeningReadiness = categoryMasteries.getOrDefault("LISTENING", overallMastery);
        BigDecimal readingReadiness = categoryMasteries.getOrDefault("READING", overallMastery);

        // Count completed sessions for this user (approximate from existing snapshots)
        int sessionsCompleted = (int) readinessSnapshotRepository.findFirstByUserIdOrderBySnapshotDateDesc(userId)
                .map(s -> s.getSessionsCompleted() != null ? s.getSessionsCompleted() + 1 : 1)
                .orElse(1);

        EilReadinessSnapshotEntity snapshot = EilReadinessSnapshotEntity.builder()
                .userId(userId)
                .testType(testType != null ? testType : "GENERAL")
                .targetScore(targetScore)
                .overallReadiness(overallMastery)
                .listeningReadiness(listeningReadiness)
                .readingReadiness(readingReadiness)
                .categoryReadiness(categoryReadinessJson)
                .predictedScore(predictedScore)
                .passProbability(passProbability)
                .gapToTarget(gapToTarget)
                .questionsAnswered(questionsAnswered)
                .sessionsCompleted(sessionsCompleted)
                .mocksCompleted(0)
                .prioritySkills(prioritySkillsJson)
                .estimatedDaysToReady(estimatedDaysToReady)
                .snapshotDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        snapshot = readinessSnapshotRepository.save(snapshot);
        log.info("Created readiness snapshot {} for user {}: overallReadiness={}, predictedScore={}",
                snapshot.getId(), userId, overallMastery, predictedScore);

        return snapshot;
    }

    /**
     * Calculate predicted score based on mastery (TOEIC scale: 10-990).
     */
    private int calculatePredictedScore(BigDecimal mastery) {
        if (mastery == null) {
            return 500; // Default mid-range
        }
        // Map mastery (0-1) to TOEIC score (10-990)
        double value = mastery.doubleValue();
        int score = (int) (10 + value * 980);
        // Round to nearest 5
        return (score / 5) * 5;
    }

    /**
     * Calculate pass probability using logistic function.
     */
    private BigDecimal calculatePassProbability(int predictedScore, int targetScore) {
        // Logistic function: P = 1 / (1 + e^(-k*(x-threshold)))
        // k controls steepness, higher k = sharper transition
        double k = 0.02;
        double prob = 1.0 / (1.0 + Math.exp(-k * (predictedScore - targetScore)));
        return BigDecimal.valueOf(prob).setScale(4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Estimate days to reach target readiness.
     */
    private int estimateDaysToReady(int gapToTarget, BigDecimal currentMastery) {
        if (gapToTarget <= 0) {
            return 0;
        }
        // Rough estimate: 1 point improvement per 0.5 days of focused practice
        // Adjusted by current mastery (higher mastery = slower progress)
        double masteryFactor = currentMastery != null ? 1.0 + currentMastery.doubleValue() : 1.5;
        return (int) Math.ceil(gapToTarget * 0.5 * masteryFactor / 10);
    }

    /**
     * Convert object to JSON string.
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON", e);
            return null;
        }
    }
}

