package com.hth.udecareer.eil.algorithm;

import com.hth.udecareer.eil.enums.DifficultyLevel;
import com.hth.udecareer.eil.enums.SessionType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects questions for adaptive practice based on user's skill mastery levels.
 *
 * The algorithm prioritizes:
 * 1. Weak skills (mastery < 0.5) to help users improve
 * 2. Skills that haven't been practiced recently
 * 3. Questions matching the user's Zone of Proximal Development (ZPD)
 * 4. Avoiding recently seen questions
 *
 * For diagnostic tests, ensures coverage across all skill categories.
 */
@Slf4j
@Component
public class QuestionSelector {

    private final MasteryCalculator masteryCalculator;

    // How many recent questions to avoid
    private static final int RECENT_QUESTIONS_BUFFER = 50;

    // Mastery thresholds
    private static final double WEAK_THRESHOLD = 0.4;
    private static final double STRONG_THRESHOLD = 0.8;

    public QuestionSelector(MasteryCalculator masteryCalculator) {
        this.masteryCalculator = masteryCalculator;
    }

    /**
     * Select the next skill to practice based on mastery levels.
     *
     * @param skillMasteries Map of skillId to mastery level
     * @param sessionType    Type of practice session
     * @param targetSkillId  Optional target skill for SKILL_FOCUS sessions
     * @param recentSkillIds Skills practiced recently (to avoid)
     * @return Selected skill ID
     */
    public Long selectNextSkill(
            Map<Long, BigDecimal> skillMasteries,
            SessionType sessionType,
            Long targetSkillId,
            Set<Long> recentSkillIds) {

        if (skillMasteries == null || skillMasteries.isEmpty()) {
            log.warn("No skill masteries provided for selection");
            return null;
        }

        // For SKILL_FOCUS sessions, always return the target skill
        if (sessionType == SessionType.SKILL_FOCUS && targetSkillId != null) {
            return targetSkillId;
        }

        // Sort skills by selection priority
        List<SkillPriority> priorities = skillMasteries.entrySet().stream()
                .map(e -> calculateSkillPriority(e.getKey(), e.getValue(), recentSkillIds))
                .sorted(Comparator.comparingDouble(SkillPriority::getPriority).reversed())
                .collect(Collectors.toList());

        if (priorities.isEmpty()) {
            return null;
        }

        // For REVIEW sessions, focus on strong skills to maintain them
        if (sessionType == SessionType.REVIEW) {
            return priorities.stream()
                    .filter(p -> p.getMastery() >= STRONG_THRESHOLD)
                    .findFirst()
                    .map(SkillPriority::getSkillId)
                    .orElse(priorities.get(0).getSkillId());
        }

        // For ADAPTIVE and MIXED, use weighted random selection from top priorities
        // This adds variety while still focusing on weak skills
        return selectWeightedRandom(priorities.subList(0, Math.min(5, priorities.size())));
    }

    /**
     * Calculate priority score for a skill.
     * Higher priority = should be practiced next.
     */
    private SkillPriority calculateSkillPriority(Long skillId, BigDecimal mastery, Set<Long> recentSkillIds) {
        double masteryValue = mastery != null ? mastery.doubleValue() : 0.5;

        // Base priority inversely proportional to mastery
        // Weak skills (0.0-0.4) get priority 1.0-0.6
        // Medium skills (0.4-0.6) get priority 0.6-0.4
        // Strong skills (0.6-1.0) get priority 0.4-0.0
        double priority = 1.0 - masteryValue;

        // Bonus for very weak skills
        if (masteryValue < WEAK_THRESHOLD) {
            priority += 0.3;
        }

        // Penalty for recently practiced skills
        if (recentSkillIds != null && recentSkillIds.contains(skillId)) {
            priority -= 0.5;
        }

        return SkillPriority.builder()
                .skillId(skillId)
                .mastery(masteryValue)
                .priority(priority)
                .build();
    }

    /**
     * Select a skill using weighted random selection.
     * Skills with higher priority have higher chance of selection.
     */
    private Long selectWeightedRandom(List<SkillPriority> priorities) {
        if (priorities.isEmpty()) {
            return null;
        }

        // Normalize priorities to get weights
        double totalPriority = priorities.stream()
                .mapToDouble(SkillPriority::getPriority)
                .sum();

        if (totalPriority <= 0) {
            return priorities.get(0).getSkillId();
        }

        double random = Math.random() * totalPriority;
        double cumulative = 0;

        for (SkillPriority p : priorities) {
            cumulative += p.getPriority();
            if (random <= cumulative) {
                return p.getSkillId();
            }
        }

        return priorities.get(priorities.size() - 1).getSkillId();
    }

    /**
     * Select target difficulty for a question based on mastery.
     *
     * @param mastery Current mastery level for the skill
     * @return Target difficulty level
     */
    public DifficultyLevel selectTargetDifficulty(BigDecimal mastery) {
        return masteryCalculator.getTargetDifficulty(mastery);
    }

    /**
     * Filter out recently seen questions.
     *
     * @param candidateIds     All candidate question IDs
     * @param recentQuestionIds Recently seen question IDs
     * @return Filtered list of question IDs
     */
    public List<Long> filterRecentQuestions(List<Long> candidateIds, Set<Long> recentQuestionIds) {
        if (candidateIds == null || candidateIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (recentQuestionIds == null || recentQuestionIds.isEmpty()) {
            return candidateIds;
        }

        List<Long> filtered = candidateIds.stream()
                .filter(id -> !recentQuestionIds.contains(id))
                .collect(Collectors.toList());

        // If filtering removes all questions, return some candidates anyway
        if (filtered.isEmpty()) {
            log.warn("All candidate questions have been seen recently, returning original list");
            return candidateIds;
        }

        return filtered;
    }

    /**
     * Select questions for diagnostic test ensuring skill coverage.
     *
     * @param skillQuestionMap   Map of skillId to list of question IDs
     * @param questionsPerSkill  Number of questions per skill
     * @param totalQuestions     Total questions to select
     * @return List of selected question IDs
     */
    public List<Long> selectDiagnosticQuestions(
            Map<Long, List<Long>> skillQuestionMap,
            int questionsPerSkill,
            int totalQuestions) {

        List<Long> selectedQuestions = new ArrayList<>();
        Set<Long> usedQuestions = new HashSet<>();

        // First pass: select questionsPerSkill from each skill
        for (Map.Entry<Long, List<Long>> entry : skillQuestionMap.entrySet()) {
            List<Long> questions = entry.getValue();
            if (questions == null || questions.isEmpty()) {
                continue;
            }

            // Shuffle and select
            List<Long> shuffled = new ArrayList<>(questions);
            Collections.shuffle(shuffled);

            int count = 0;
            for (Long qId : shuffled) {
                if (!usedQuestions.contains(qId) && count < questionsPerSkill) {
                    selectedQuestions.add(qId);
                    usedQuestions.add(qId);
                    count++;
                }
            }
        }

        // If we need more questions, add randomly from remaining
        if (selectedQuestions.size() < totalQuestions) {
            List<Long> remainingQuestions = skillQuestionMap.values().stream()
                    .flatMap(List::stream)
                    .filter(id -> !usedQuestions.contains(id))
                    .collect(Collectors.toList());

            Collections.shuffle(remainingQuestions);

            for (Long qId : remainingQuestions) {
                if (selectedQuestions.size() >= totalQuestions) {
                    break;
                }
                selectedQuestions.add(qId);
            }
        }

        // Shuffle final list for randomized order
        Collections.shuffle(selectedQuestions);

        // Trim to total questions
        if (selectedQuestions.size() > totalQuestions) {
            selectedQuestions = selectedQuestions.subList(0, totalQuestions);
        }

        log.debug("Selected {} diagnostic questions from {} skills",
                selectedQuestions.size(), skillQuestionMap.size());

        return selectedQuestions;
    }

    /**
     * Get the number of recent questions to track for avoidance.
     *
     * @return Buffer size
     */
    public int getRecentQuestionsBufferSize() {
        return RECENT_QUESTIONS_BUFFER;
    }

    /**
     * Identify weak skills that need improvement.
     *
     * @param skillMasteries Map of skillId to mastery level
     * @param limit          Maximum number of skills to return
     * @return List of weak skill IDs sorted by mastery (lowest first)
     */
    public List<Long> identifyWeakSkills(Map<Long, BigDecimal> skillMasteries, int limit) {
        if (skillMasteries == null || skillMasteries.isEmpty()) {
            return Collections.emptyList();
        }

        return skillMasteries.entrySet().stream()
                .filter(e -> e.getValue() == null || e.getValue().doubleValue() < WEAK_THRESHOLD)
                .sorted(Comparator.comparing(e -> e.getValue() != null ? e.getValue() : BigDecimal.ZERO))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Identify strong skills.
     *
     * @param skillMasteries Map of skillId to mastery level
     * @param limit          Maximum number of skills to return
     * @return List of strong skill IDs sorted by mastery (highest first)
     */
    public List<Long> identifyStrongSkills(Map<Long, BigDecimal> skillMasteries, int limit) {
        if (skillMasteries == null || skillMasteries.isEmpty()) {
            return Collections.emptyList();
        }

        return skillMasteries.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().doubleValue() >= STRONG_THRESHOLD)
                .sorted(Comparator.comparing(Map.Entry<Long, BigDecimal>::getValue).reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Data
    @Builder
    private static class SkillPriority {
        private Long skillId;
        private double mastery;
        private double priority;
    }
}
