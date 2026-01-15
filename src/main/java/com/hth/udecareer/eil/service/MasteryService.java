package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.algorithm.MasteryCalculator;
import com.hth.udecareer.eil.entities.EilSkillEntity;
import com.hth.udecareer.eil.entities.EilSkillMasteryEntity;
import com.hth.udecareer.eil.enums.DifficultyLevel;
import com.hth.udecareer.eil.enums.EstimatedLevel;
import com.hth.udecareer.eil.model.dto.WeakSkillDto;
import com.hth.udecareer.eil.model.response.SkillMapResponse;
import com.hth.udecareer.eil.model.response.SkillMasteryResponse;
import com.hth.udecareer.eil.repository.EilSkillMasteryRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user skill mastery levels.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MasteryService {

    private final EilSkillMasteryRepository masteryRepository;
    private final SkillService skillService;
    private final MasteryCalculator masteryCalculator;

    private static final int SCALE = 4;
    private static final BigDecimal WEAK_THRESHOLD = new BigDecimal("0.4000");
    private static final BigDecimal STRONG_THRESHOLD = new BigDecimal("0.8000");

    /**
     * Get or create mastery record for a user and skill.
     */
    @Transactional
    public EilSkillMasteryEntity getOrCreateMastery(Long userId, Long skillId) {
        return masteryRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseGet(() -> initializeMastery(userId, skillId));
    }

    /**
     * Initialize mastery for a new skill.
     */
    @Transactional
    public EilSkillMasteryEntity initializeMastery(Long userId, Long skillId) {
        EilSkillMasteryEntity mastery = EilSkillMasteryEntity.builder()
                .userId(userId)
                .skillId(skillId)
                .masteryLevel(masteryCalculator.getInitialMastery())
                .confidence(new BigDecimal("0.1000"))
                .attempts(0)
                .correctCount(0)
                .streak(0)
                .lastPracticedAt(LocalDateTime.now())
                .build();

        return masteryRepository.save(mastery);
    }

    /**
     * Initialize mastery for all skills for a user.
     */
    @Transactional
    public void initializeAllMasteries(Long userId) {
        List<EilSkillEntity> leafSkills = skillService.getLeafSkills();
        List<Long> existingSkillIds = masteryRepository.findByUserId(userId).stream()
                .map(EilSkillMasteryEntity::getSkillId)
                .collect(Collectors.toList());

        List<EilSkillMasteryEntity> newMasteries = leafSkills.stream()
                .filter(skill -> !existingSkillIds.contains(skill.getId()))
                .map(skill -> EilSkillMasteryEntity.builder()
                        .userId(userId)
                        .skillId(skill.getId())
                        .masteryLevel(masteryCalculator.getInitialMastery())
                        .confidence(new BigDecimal("0.1000"))
                        .attempts(0)
                        .correctCount(0)
                        .streak(0)
                        .build())
                .collect(Collectors.toList());

        if (!newMasteries.isEmpty()) {
            masteryRepository.saveAll(newMasteries);
            log.info("Initialized {} mastery records for user {}", newMasteries.size(), userId);
        }
    }

    /**
     * Update mastery after an attempt.
     */
    @Transactional
    public MasteryUpdateResult updateMastery(Long userId, Long skillId, boolean isCorrect, DifficultyLevel difficulty) {
        EilSkillMasteryEntity mastery = getOrCreateMastery(userId, skillId);

        BigDecimal oldMastery = mastery.getMasteryLevel();

        // Calculate new mastery using EMA algorithm
        BigDecimal newMastery = masteryCalculator.calculateNewMastery(
                oldMastery,
                isCorrect,
                difficulty,
                mastery.getAttempts()
        );

        BigDecimal delta = masteryCalculator.calculateDelta(oldMastery, newMastery);

        // Update stats
        mastery.setMasteryLevel(newMastery);
        mastery.setAttempts(mastery.getAttempts() + 1);

        int correctIncrement = isCorrect ? 1 : 0;
        mastery.setCorrectCount(mastery.getCorrectCount() + correctIncrement);

        // Update streak (positive for correct, negative for wrong)
        if (isCorrect) {
            mastery.setStreak(Math.max(0, mastery.getStreak()) + 1);
        } else {
            mastery.setStreak(Math.min(0, mastery.getStreak()) - 1);
        }

        // Update confidence (increases with more attempts)
        BigDecimal newConfidence = calculateConfidence(mastery.getAttempts());
        mastery.setConfidence(newConfidence);

        mastery.setLastPracticedAt(LocalDateTime.now());

        masteryRepository.save(mastery);

        log.debug("Updated mastery for user {} skill {}: {} -> {} (delta: {})",
                userId, skillId, oldMastery, newMastery, delta);

        return MasteryUpdateResult.builder()
                .skillId(skillId)
                .masteryBefore(oldMastery)
                .masteryAfter(newMastery)
                .masteryDelta(delta)
                .build();
    }

    /**
     * Calculate confidence based on number of attempts.
     */
    private BigDecimal calculateConfidence(int attempts) {
        // Confidence grows logarithmically with attempts, maxing at 0.95
        double confidence = Math.min(0.95, 0.1 + 0.85 * (1 - 1.0 / (1 + Math.log(1 + attempts))));
        return BigDecimal.valueOf(confidence).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Get all masteries for a user.
     */
    public List<EilSkillMasteryEntity> getUserMasteries(Long userId) {
        return masteryRepository.findByUserId(userId);
    }

    /**
     * Get mastery map (skillId -> mastery level) for a user.
     */
    public Map<Long, BigDecimal> getMasteryMap(Long userId) {
        return masteryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        EilSkillMasteryEntity::getSkillId,
                        EilSkillMasteryEntity::getMasteryLevel
                ));
    }

    /**
     * Get weak skills for a user.
     */
    public List<WeakSkillDto> getWeakSkills(Long userId, int limit) {
        List<EilSkillMasteryEntity> weakMasteries = masteryRepository.findWeakSkills(userId, WEAK_THRESHOLD);

        // Limit results
        if (weakMasteries.size() > limit) {
            weakMasteries = weakMasteries.subList(0, limit);
        }

        int rank = 1;
        List<WeakSkillDto> result = new ArrayList<>();

        for (EilSkillMasteryEntity m : weakMasteries) {
            EilSkillEntity skill = null;
            try {
                skill = skillService.getSkillById(m.getSkillId());
            } catch (AppException e) {
                log.warn("Skill not found: {}", m.getSkillId());
            }

            double accuracy = m.getAttempts() > 0
                    ? (m.getCorrectCount() * 100.0 / m.getAttempts())
                    : 0.0;

            WeakSkillDto dto = WeakSkillDto.builder()
                    .skillId(m.getSkillId())
                    .skillCode(skill != null ? skill.getCode() : null)
                    .skillName(skill != null ? skill.getName() : null)
                    .skillNameVi(skill != null ? skill.getNameVi() : null)
                    .category(skill != null ? skill.getCategory() : null)
                    .subcategory(skill != null ? skill.getSubcategory() : null)
                    .masteryLevel(m.getMasteryLevel().doubleValue())
                    .masteryLabel(masteryCalculator.getMasteryLabel(m.getMasteryLevel()).name())
                    .attempts(m.getAttempts())
                    .accuracy(accuracy)
                    .priorityRank(rank++)
                    .build();

            result.add(dto);
        }

        return result;
    }

    /**
     * Get all skill results for user (for diagnostic results display).
     */
    public List<SkillMasteryResponse> getAllSkillResults(Long userId, int limit) {
        List<EilSkillMasteryEntity> masteries = getUserMasteries(userId);

        return masteries.stream()
                .limit(limit)
                .map(this::toSkillMasteryResponse)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Get skill results filtered by certification.
     *
     * IMPORTANT: This method filters skills to only return those belonging to the specified certification.
     * This prevents cross-contamination when a user has completed diagnostics for multiple certifications.
     *
     * NOTE: System has 2 skill tables:
     * - eil_skills (legacy, for TOEIC/English tests) - NO certification_id field
     * - wp_ez_skills (new, for ISTQB/PSM/etc.) - HAS certification_id field
     *
     * This method checks category field (from eil_skills) as a fallback for filtering.
     *
     * @param userId User ID
     * @param certificationId Certification ID (e.g., "ISTQB_CTFL", "PSM_I")
     * @param limit Max number of results
     * @return List of skill mastery responses for the specified certification only
     */
    public List<SkillMasteryResponse> getSkillResultsByCertification(
            Long userId,
            String certificationId,
            int limit) {

        if (certificationId == null || certificationId.isEmpty()) {
            log.warn("certificationId is null or empty, falling back to getAllSkillResults");
            return getAllSkillResults(userId, limit);
        }

        List<EilSkillMasteryEntity> masteries = getUserMasteries(userId);

        log.debug("Filtering {} masteries for certificationId: {}", masteries.size(), certificationId);

        // Get skills belonging to this certification
        // Strategy: Get skills from wp_ez_skills that match certification_id
        List<Long> certificationSkillIds = skillService.getSkillIdsByCertification(certificationId);

        return masteries.stream()
                .filter(mastery -> {
                    // Check if skill belongs to the certification
                    boolean matches = certificationSkillIds.contains(mastery.getSkillId());

                    if (!matches) {
                        log.trace("Skipping skill {} - not in certification {}",
                                mastery.getSkillId(), certificationId);
                    }

                    return matches;
                })
                .limit(limit)
                .map(this::toSkillMasteryResponse)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Get category-level mastery averages.
     */
    public Map<String, BigDecimal> getCategoryMasteries(Long userId) {
        List<String> categories = skillService.getAllActiveCategories();
        Map<String, BigDecimal> result = new HashMap<>();

        for (String category : categories) {
            BigDecimal avgMastery = masteryRepository.getAverageMasteryByUserIdAndCategory(userId, category);
            result.put(category, avgMastery != null ? avgMastery : masteryCalculator.getInitialMastery());
        }

        return result;
    }

    /**
     * Get overall mastery for a user.
     */
    public BigDecimal getOverallMastery(Long userId) {
        BigDecimal avg = masteryRepository.getAverageMasteryByUserId(userId);
        return avg != null ? avg : masteryCalculator.getInitialMastery();
    }

    /**
     * Get estimated level based on overall mastery.
     */
    public EstimatedLevel getEstimatedLevel(BigDecimal overallMastery) {
        if (overallMastery == null) {
            return EstimatedLevel.BEGINNER;
        }

        double value = overallMastery.doubleValue();
        if (value < 0.3) {
            return EstimatedLevel.BEGINNER;
        } else if (value < 0.5) {
            return EstimatedLevel.ELEMENTARY;
        } else if (value < 0.65) {
            return EstimatedLevel.INTERMEDIATE;
        } else if (value < 0.8) {
            return EstimatedLevel.UPPER_INTERMEDIATE;
        } else {
            return EstimatedLevel.ADVANCED;
        }
    }

    /**
     * Build complete skill map response for a user.
     */
    public SkillMapResponse buildSkillMap(Long userId) {
        List<EilSkillMasteryEntity> masteries = getUserMasteries(userId);

        if (masteries.isEmpty()) {
            // Initialize masteries if user has none
            initializeAllMasteries(userId);
            masteries = getUserMasteries(userId);
        }

        Map<String, BigDecimal> categoryMasteries = getCategoryMasteries(userId);
        BigDecimal overallMastery = getOverallMastery(userId);
        EstimatedLevel estimatedLevel = getEstimatedLevel(overallMastery);

        // Get weak and strong skills
        List<SkillMasteryResponse> weakSkills = getWeakSkills(userId, 5).stream()
                .map(this::toSkillMasteryResponse)
                .collect(Collectors.toList());

        List<SkillMasteryResponse> allSkillResponses = masteries.stream()
                .map(this::toSkillMasteryResponse)
                .collect(Collectors.toList());

        List<SkillMasteryResponse> strongSkills = allSkillResponses.stream()
                .filter(s -> s.getMasteryLevel() != null && s.getMasteryLevel() >= 0.8)
                .sorted(Comparator.comparing(SkillMasteryResponse::getMasteryLevel).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Calculate estimated score
        SkillMapResponse.ScoreRange scoreRange = calculateScoreRange(overallMastery);

        // Get total questions answered
        Long totalAttempts = masteryRepository.getTotalAttemptsByUserId(userId);

        return SkillMapResponse.builder()
                .userId(userId)
                .testType("TOEIC")
                .overallMastery(overallMastery)
                .estimatedLevel(estimatedLevel)
                .estimatedScore(scoreRange)
                .categoryMastery(SkillMapResponse.CategoryMastery.builder()
                        .listening(categoryMasteries.get("LISTENING"))
                        .reading(categoryMasteries.get("READING"))
                        .grammar(categoryMasteries.get("GRAMMAR"))
                        .vocabulary(categoryMasteries.get("VOCABULARY"))
                        .build())
                .skills(allSkillResponses)
                .weakSkills(weakSkills)
                .strongSkills(strongSkills)
                .diagnosticCompleted(!masteries.isEmpty() && masteries.stream().anyMatch(m -> m.getAttempts() > 0))
                .totalQuestionsAnswered(totalAttempts != null ? totalAttempts.intValue() : 0)
                .lastUpdatedAt(masteries.stream()
                        .map(EilSkillMasteryEntity::getLastPracticedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null))
                .build();
    }

    /**
     * Convert weak skill DTO to skill mastery response.
     */
    private SkillMasteryResponse toSkillMasteryResponse(WeakSkillDto dto) {
        return SkillMasteryResponse.builder()
                .skillId(dto.getSkillId())
                .skillCode(dto.getSkillCode())
                .skillName(dto.getSkillName())
                .skillNameVi(dto.getSkillNameVi())
                .category(dto.getCategory())
                .subcategory(dto.getSubcategory())
                .masteryLevel(dto.getMasteryLevel())
                .masteryLabel(dto.getMasteryLabel())
                .attempts(dto.getAttempts())
                .build();
    }

    /**
     * Convert mastery entity to response.
     */
    private SkillMasteryResponse toSkillMasteryResponse(EilSkillMasteryEntity entity) {
        EilSkillEntity skill = null;
        try {
            skill = skillService.getSkillById(entity.getSkillId());
        } catch (AppException e) {
            log.warn("Skill not found: {}", entity.getSkillId());
        }

        return SkillMasteryResponse.builder()
                .skillId(entity.getSkillId())
                .skillCode(skill != null ? skill.getCode() : null)
                .skillName(skill != null ? skill.getName() : null)
                .skillNameVi(skill != null ? skill.getNameVi() : null)
                .category(skill != null ? skill.getCategory() : null)
                .subcategory(skill != null ? skill.getSubcategory() : null)
                .masteryLevel(entity.getMasteryLevel().doubleValue())
                .confidence(entity.getConfidence() != null ? entity.getConfidence().doubleValue() : null)
                .masteryLabel(masteryCalculator.getMasteryLabel(entity.getMasteryLevel()).name())
                .attempts(entity.getAttempts())
                .correctCount(entity.getCorrectCount())
                .streak(entity.getStreak())
                .lastPracticed(entity.getLastPracticedAt())
                .build();
    }

    /**
     * Calculate estimated score range based on mastery.
     */
    private SkillMapResponse.ScoreRange calculateScoreRange(BigDecimal mastery) {
        double value = mastery != null ? mastery.doubleValue() : 0.5;

        // TOEIC score mapping (10-990)
        int midScore = (int) (10 + value * 980);
        int minScore = Math.max(10, midScore - 50);
        int maxScore = Math.min(990, midScore + 50);

        // Round to nearest 5
        midScore = (midScore / 5) * 5;
        minScore = (minScore / 5) * 5;
        maxScore = (maxScore / 5) * 5;

        return SkillMapResponse.ScoreRange.builder()
                .min(minScore)
                .max(maxScore)
                .mid(midScore)
                .build();
    }

    /**
     * Result of mastery update operation.
     */
    @lombok.Builder
    @lombok.Data
    public static class MasteryUpdateResult {
        private Long skillId;
        private BigDecimal masteryBefore;
        private BigDecimal masteryAfter;
        private BigDecimal masteryDelta;
    }
}
