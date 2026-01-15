package com.hth.udecareer.eil.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.eil.entities.EilTimeEstimateEntity;
import com.hth.udecareer.eil.model.request.TimeEstimateRequest;
import com.hth.udecareer.eil.model.response.TimeEstimateResponse;
import com.hth.udecareer.eil.repository.EilSkillMasteryRepository;
import com.hth.udecareer.eil.repository.EilTimeEstimateRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Time-to-Certification estimation.
 * Tracks progress and estimates time needed to achieve certification readiness.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeEstimationService {

    private final EilTimeEstimateRepository timeEstimateRepository;
    private final EilSkillMasteryRepository skillMasteryRepository;
    private final ObjectMapper objectMapper;

    // Default values
    private static final BigDecimal DEFAULT_TARGET_MASTERY = new BigDecimal("0.8000");
    private static final BigDecimal DEFAULT_RECOMMENDED_HOURS = new BigDecimal("2.00");
    private static final int HOURS_PER_SESSION = 1;
    private static final double MASTERY_GAIN_PER_HOUR = 0.02; // 2% per hour on average

    /**
     * Create a new time estimate for a certification.
     */
    @Transactional
    public TimeEstimateResponse createEstimate(Long userId, TimeEstimateRequest request) {
        // Check if estimate already exists
        Optional<EilTimeEstimateEntity> existing = timeEstimateRepository
                .findByUserIdAndCertificationCode(userId, request.getCertificationCode());

        if (existing.isPresent()) {
            log.info("Time estimate already exists for user {} cert {}", userId, request.getCertificationCode());
            return entityToResponse(existing.get());
        }

        // Get current mastery
        BigDecimal currentMastery = calculateCurrentMastery(userId, request.getCertificationCode());
        BigDecimal targetMastery = request.getTargetMastery() != null ?
                request.getTargetMastery() : DEFAULT_TARGET_MASTERY;
        BigDecimal masteryGap = targetMastery.subtract(currentMastery).max(BigDecimal.ZERO);

        // Calculate estimates
        BigDecimal recommendedHours = request.getRecommendedDailyHours() != null ?
                request.getRecommendedDailyHours() : DEFAULT_RECOMMENDED_HOURS;

        int estimatedHours = calculateEstimatedHours(masteryGap);
        int estimatedDays = calculateEstimatedDays(estimatedHours, recommendedHours);
        int estimatedSessions = estimatedHours / HOURS_PER_SESSION;
        LocalDate estimatedReadyDate = LocalDate.now().plusDays(estimatedDays);

        // Calculate confidence based on data available
        BigDecimal confidence = calculateConfidence(userId, 0);
        String confidenceLevel = getConfidenceLevel(confidence);

        EilTimeEstimateEntity entity = EilTimeEstimateEntity.builder()
                .userId(userId)
                .certificationCode(request.getCertificationCode())
                .certificationName(request.getCertificationName())
                .currentMastery(currentMastery)
                .targetMastery(targetMastery)
                .masteryGap(masteryGap)
                .estimatedDays(estimatedDays)
                .estimatedHours(estimatedHours)
                .estimatedSessions(estimatedSessions)
                .estimatedReadyDate(estimatedReadyDate)
                .daysPracticed(0)
                .totalStudyHours(BigDecimal.ZERO)
                .questionsPracticed(0)
                .sessionsCompleted(0)
                .recommendedDailyHours(recommendedHours)
                .confidence(confidence)
                .confidenceLevel(confidenceLevel)
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .targetDate(request.getTargetDate())
                .lastActivityAt(LocalDateTime.now())
                .build();

        entity = timeEstimateRepository.save(entity);
        log.info("Created time estimate {} for user {} cert {}", entity.getId(), userId, request.getCertificationCode());

        return entityToResponse(entity);
    }

    /**
     * Get estimate for a certification.
     */
    public TimeEstimateResponse getEstimate(Long userId, String certificationCode) {
        EilTimeEstimateEntity entity = timeEstimateRepository
                .findByUserIdAndCertificationCode(userId, certificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return entityToResponse(entity);
    }

    /**
     * Get all estimates for a user.
     */
    public List<TimeEstimateResponse> getAllEstimates(Long userId) {
        List<EilTimeEstimateEntity> entities = timeEstimateRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return entities.stream().map(this::entityToResponse).collect(Collectors.toList());
    }

    /**
     * Get active estimates.
     */
    public List<TimeEstimateResponse> getActiveEstimates(Long userId) {
        List<EilTimeEstimateEntity> entities = timeEstimateRepository.findActiveEstimates(userId);
        return entities.stream().map(this::entityToResponse).collect(Collectors.toList());
    }

    /**
     * Update progress on an estimate.
     */
    @Transactional
    public TimeEstimateResponse updateProgress(Long userId, String certificationCode,
                                                TimeEstimateRequest.ProgressUpdateRequest request) {
        EilTimeEstimateEntity entity = timeEstimateRepository
                .findByUserIdAndCertificationCode(userId, certificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Update progress
        if (request.getStudyHours() != null) {
            entity.setTotalStudyHours(entity.getTotalStudyHours().add(request.getStudyHours()));
        }
        if (request.getQuestionsPracticed() != null) {
            entity.setQuestionsPracticed(entity.getQuestionsPracticed() + request.getQuestionsPracticed());
        }
        if (Boolean.TRUE.equals(request.getSessionCompleted())) {
            entity.setSessionsCompleted(entity.getSessionsCompleted() + 1);
        }

        // Update mastery if provided
        if (request.getNewMastery() != null) {
            BigDecimal oldMastery = entity.getCurrentMastery();
            entity.setCurrentMastery(request.getNewMastery());
            entity.setMasteryGap(entity.getTargetMastery().subtract(request.getNewMastery()).max(BigDecimal.ZERO));

            // Calculate mastery velocity
            if (entity.getDaysPracticed() > 0) {
                BigDecimal masteryGain = request.getNewMastery().subtract(oldMastery != null ? oldMastery : BigDecimal.ZERO);
                entity.setMasteryVelocity(masteryGain.divide(BigDecimal.valueOf(entity.getDaysPracticed()), 5, RoundingMode.HALF_UP));
            }
        }

        // Update days practiced
        LocalDate today = LocalDate.now();
        if (entity.getLastActivityAt() == null || !entity.getLastActivityAt().toLocalDate().equals(today)) {
            entity.setDaysPracticed(entity.getDaysPracticed() + 1);
        }

        entity.setLastActivityAt(LocalDateTime.now());

        // Recalculate pace and estimates
        recalculatePace(entity);
        recalculateEstimates(entity);

        // Update confidence
        entity.setConfidence(calculateConfidence(userId, entity.getDaysPracticed()));
        entity.setConfidenceLevel(getConfidenceLevel(entity.getConfidence()));

        // Check if completed
        if (entity.getCurrentMastery() != null &&
                entity.getCurrentMastery().compareTo(entity.getTargetMastery()) >= 0) {
            entity.setStatus("COMPLETED");
            log.info("User {} completed certification {} goal", userId, certificationCode);
        }

        // Update progress history
        updateProgressHistory(entity);

        entity = timeEstimateRepository.save(entity);
        return entityToResponse(entity);
    }

    /**
     * Update estimate status.
     */
    @Transactional
    public TimeEstimateResponse updateStatus(Long userId, String certificationCode, String status) {
        EilTimeEstimateEntity entity = timeEstimateRepository
                .findByUserIdAndCertificationCode(userId, certificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        entity.setStatus(status);
        entity = timeEstimateRepository.save(entity);
        log.info("Updated estimate status to {} for user {} cert {}", status, userId, certificationCode);

        return entityToResponse(entity);
    }

    /**
     * Get pace analysis.
     */
    public TimeEstimateResponse.PaceAnalysis getPaceAnalysis(Long userId, String certificationCode) {
        EilTimeEstimateEntity entity = timeEstimateRepository
                .findByUserIdAndCertificationCode(userId, certificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        BigDecimal currentPace = entity.getAvgDailyHours() != null ? entity.getAvgDailyHours() : BigDecimal.ZERO;

        // Calculate required pace to meet target date
        BigDecimal requiredPace = BigDecimal.ZERO;
        int daysRemaining = 0;
        LocalDate projectedCompletion = null;

        if (entity.getTargetDate() != null) {
            daysRemaining = (int) ChronoUnit.DAYS.between(LocalDate.now(), entity.getTargetDate());
            if (daysRemaining > 0 && entity.getMasteryGap() != null) {
                double hoursNeeded = entity.getMasteryGap().doubleValue() / MASTERY_GAIN_PER_HOUR;
                requiredPace = BigDecimal.valueOf(hoursNeeded / daysRemaining).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Calculate projected completion
        if (currentPace.compareTo(BigDecimal.ZERO) > 0 && entity.getMasteryGap() != null) {
            double hoursNeeded = entity.getMasteryGap().doubleValue() / MASTERY_GAIN_PER_HOUR;
            int daysNeeded = (int) Math.ceil(hoursNeeded / currentPace.doubleValue());
            projectedCompletion = LocalDate.now().plusDays(daysNeeded);
        }

        BigDecimal paceDifference = currentPace.subtract(requiredPace);
        boolean needsAcceleration = paceDifference.compareTo(BigDecimal.ZERO) < 0;

        return TimeEstimateResponse.PaceAnalysis.builder()
                .currentPace(currentPace)
                .requiredPace(requiredPace)
                .paceDifference(paceDifference)
                .needsAcceleration(needsAcceleration)
                .suggestedDailyHours(needsAcceleration ? requiredPace.add(new BigDecimal("0.5")) : currentPace)
                .daysRemaining(daysRemaining)
                .projectedCompletionDate(projectedCompletion)
                .build();
    }

    /**
     * Delete estimate.
     */
    @Transactional
    public void deleteEstimate(Long userId, String certificationCode) {
        timeEstimateRepository.deleteByUserIdAndCertificationCode(userId, certificationCode);
        log.info("Deleted time estimate for user {} cert {}", userId, certificationCode);
    }

    // ==================== Private Helper Methods ====================

    private BigDecimal calculateCurrentMastery(Long userId, String certificationCode) {
        BigDecimal avgMastery = skillMasteryRepository.getAverageMasteryByUserIdAndCategory(userId, certificationCode);
        if (avgMastery == null) {
            avgMastery = skillMasteryRepository.getAverageMasteryByUserId(userId);
        }
        return avgMastery != null ? avgMastery : BigDecimal.ZERO;
    }

    private int calculateEstimatedHours(BigDecimal masteryGap) {
        if (masteryGap == null || masteryGap.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return (int) Math.ceil(masteryGap.doubleValue() / MASTERY_GAIN_PER_HOUR);
    }

    private int calculateEstimatedDays(int totalHours, BigDecimal dailyHours) {
        if (dailyHours == null || dailyHours.compareTo(BigDecimal.ZERO) <= 0) {
            return totalHours; // 1 hour per day default
        }
        return (int) Math.ceil(totalHours / dailyHours.doubleValue());
    }

    private void recalculatePace(EilTimeEstimateEntity entity) {
        if (entity.getDaysPracticed() != null && entity.getDaysPracticed() > 0) {
            BigDecimal avgDaily = entity.getTotalStudyHours()
                    .divide(BigDecimal.valueOf(entity.getDaysPracticed()), 2, RoundingMode.HALF_UP);
            entity.setAvgDailyHours(avgDaily);

            BigDecimal avgSessions = BigDecimal.valueOf(entity.getSessionsCompleted())
                    .divide(BigDecimal.valueOf(entity.getDaysPracticed()), 2, RoundingMode.HALF_UP);
            entity.setAvgSessionsPerDay(avgSessions);
        }
    }

    private void recalculateEstimates(EilTimeEstimateEntity entity) {
        if (entity.getMasteryGap() == null || entity.getMasteryGap().compareTo(BigDecimal.ZERO) <= 0) {
            entity.setEstimatedDays(0);
            entity.setEstimatedHours(0);
            entity.setEstimatedReadyDate(LocalDate.now());
            return;
        }

        // Use mastery velocity if available, otherwise use default
        double velocityPerDay = MASTERY_GAIN_PER_HOUR;
        if (entity.getMasteryVelocity() != null && entity.getMasteryVelocity().compareTo(BigDecimal.ZERO) > 0) {
            velocityPerDay = entity.getMasteryVelocity().doubleValue();
        } else if (entity.getAvgDailyHours() != null && entity.getAvgDailyHours().compareTo(BigDecimal.ZERO) > 0) {
            velocityPerDay = entity.getAvgDailyHours().doubleValue() * MASTERY_GAIN_PER_HOUR;
        }

        int daysNeeded = (int) Math.ceil(entity.getMasteryGap().doubleValue() / velocityPerDay);
        int hoursNeeded = (int) Math.ceil(entity.getMasteryGap().doubleValue() / MASTERY_GAIN_PER_HOUR);

        entity.setEstimatedDays(daysNeeded);
        entity.setEstimatedHours(hoursNeeded);
        entity.setEstimatedSessions(hoursNeeded / HOURS_PER_SESSION);
        entity.setEstimatedReadyDate(LocalDate.now().plusDays(daysNeeded));
    }

    private BigDecimal calculateConfidence(Long userId, int daysPracticed) {
        // Confidence increases with more practice days
        double base = 0.3;
        double increment = Math.min(0.05 * daysPracticed, 0.6);
        return BigDecimal.valueOf(base + increment).setScale(4, RoundingMode.HALF_UP);
    }

    private String getConfidenceLevel(BigDecimal confidence) {
        if (confidence == null) return "LOW";
        double c = confidence.doubleValue();
        if (c >= 0.7) return "HIGH";
        if (c >= 0.5) return "MEDIUM";
        return "LOW";
    }

    private void updateProgressHistory(EilTimeEstimateEntity entity) {
        try {
            List<TimeEstimateResponse.ProgressSnapshot> history;
            if (entity.getProgressHistory() != null && !entity.getProgressHistory().isEmpty()) {
                history = objectMapper.readValue(entity.getProgressHistory(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TimeEstimateResponse.ProgressSnapshot.class));
            } else {
                history = new ArrayList<>();
            }

            history.add(TimeEstimateResponse.ProgressSnapshot.builder()
                    .date(LocalDate.now())
                    .mastery(entity.getCurrentMastery())
                    .hoursStudied(entity.getTotalStudyHours())
                    .questionsAnswered(entity.getQuestionsPracticed())
                    .build());

            // Keep last 30 entries
            if (history.size() > 30) {
                history = history.subList(history.size() - 30, history.size());
            }

            entity.setProgressHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.warn("Failed to update progress history", e);
        }
    }

    private TimeEstimateResponse entityToResponse(EilTimeEstimateEntity entity) {
        List<String> focusAreas = new ArrayList<>();
        if (entity.getFocusAreas() != null && !entity.getFocusAreas().isEmpty()) {
            try {
                focusAreas = objectMapper.readValue(entity.getFocusAreas(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse focus areas", e);
            }
        }

        // Calculate progress percentage
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (entity.getTargetMastery() != null && entity.getTargetMastery().compareTo(BigDecimal.ZERO) > 0
                && entity.getCurrentMastery() != null) {
            progressPercentage = entity.getCurrentMastery()
                    .divide(entity.getTargetMastery(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .min(BigDecimal.valueOf(100));
        }

        // Determine if on track
        Boolean onTrack = null;
        if (entity.getTargetDate() != null && entity.getEstimatedReadyDate() != null) {
            onTrack = !entity.getEstimatedReadyDate().isAfter(entity.getTargetDate());
        }

        return TimeEstimateResponse.builder()
                .id(entity.getId())
                .certificationCode(entity.getCertificationCode())
                .certificationName(entity.getCertificationName())
                .currentMastery(entity.getCurrentMastery())
                .targetMastery(entity.getTargetMastery())
                .masteryGap(entity.getMasteryGap())
                .estimatedDays(entity.getEstimatedDays())
                .estimatedHours(entity.getEstimatedHours())
                .estimatedSessions(entity.getEstimatedSessions())
                .estimatedReadyDate(entity.getEstimatedReadyDate())
                .daysPracticed(entity.getDaysPracticed())
                .totalStudyHours(entity.getTotalStudyHours())
                .questionsPracticed(entity.getQuestionsPracticed())
                .sessionsCompleted(entity.getSessionsCompleted())
                .avgDailyHours(entity.getAvgDailyHours())
                .avgSessionsPerDay(entity.getAvgSessionsPerDay())
                .masteryVelocity(entity.getMasteryVelocity())
                .confidence(entity.getConfidence())
                .confidenceLevel(entity.getConfidenceLevel())
                .status(entity.getStatus())
                .recommendedDailyHours(entity.getRecommendedDailyHours())
                .focusAreas(focusAreas)
                .startDate(entity.getStartDate())
                .targetDate(entity.getTargetDate())
                .lastActivityAt(entity.getLastActivityAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .progressPercentage(progressPercentage)
                .onTrack(onTrack)
                .build();
    }
}
