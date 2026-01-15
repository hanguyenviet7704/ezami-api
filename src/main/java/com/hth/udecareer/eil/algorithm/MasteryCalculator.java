package com.hth.udecareer.eil.algorithm;

import com.hth.udecareer.eil.enums.DifficultyLevel;
import com.hth.udecareer.eil.enums.MasteryLabel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates skill mastery using Exponential Moving Average (EMA) algorithm.
 *
 * The mastery level is updated after each practice attempt based on:
 * - Whether the answer was correct
 * - The difficulty level of the question
 * - The current mastery level (confidence factor)
 *
 * Formula: newMastery = alpha * performance + (1 - alpha) * oldMastery
 *
 * Alpha (learning rate) adjusts based on number of attempts:
 * - More attempts = smaller alpha (more stable)
 * - Fewer attempts = larger alpha (faster convergence)
 */
@Slf4j
@Component
public class MasteryCalculator {

    private static final BigDecimal MIN_MASTERY = BigDecimal.ZERO;
    private static final BigDecimal MAX_MASTERY = BigDecimal.ONE;
    private static final int SCALE = 4;

    @Value("${eil.mastery.alpha:0.3}")
    private double baseAlpha;

    @Value("${eil.mastery.initial-level:0.5}")
    private double initialMasteryLevel;

    /**
     * Calculate new mastery level after an attempt.
     *
     * @param currentMastery Current mastery level (0.0-1.0)
     * @param isCorrect      Whether the answer was correct
     * @param difficulty     Difficulty level of the question
     * @param totalAttempts  Total attempts on this skill
     * @return New mastery level (0.0-1.0)
     */
    public BigDecimal calculateNewMastery(
            BigDecimal currentMastery,
            boolean isCorrect,
            DifficultyLevel difficulty,
            int totalAttempts) {

        // Calculate adaptive alpha based on number of attempts
        double alpha = calculateAdaptiveAlpha(totalAttempts);

        // Calculate performance score based on correctness and difficulty
        double performance = calculatePerformance(isCorrect, difficulty);

        // Apply EMA formula
        double current = currentMastery != null ? currentMastery.doubleValue() : initialMasteryLevel;
        double newMastery = alpha * performance + (1 - alpha) * current;

        // Clamp to [0.0, 1.0] range
        newMastery = Math.max(0.0, Math.min(1.0, newMastery));

        BigDecimal result = BigDecimal.valueOf(newMastery).setScale(SCALE, RoundingMode.HALF_UP);

        log.debug("Mastery update: current={}, isCorrect={}, difficulty={}, attempts={}, alpha={}, performance={}, new={}",
                currentMastery, isCorrect, difficulty, totalAttempts, alpha, performance, result);

        return result;
    }

    /**
     * Calculate adaptive learning rate (alpha) based on attempt count.
     * Starts high (0.5) for quick convergence, decreases to baseAlpha (0.3) for stability.
     *
     * @param totalAttempts Number of attempts on this skill
     * @return Alpha value between baseAlpha and 0.5
     */
    private double calculateAdaptiveAlpha(int totalAttempts) {
        // Alpha decreases from 0.5 to baseAlpha over first 20 attempts
        double maxAlpha = 0.5;
        double decayRate = 0.1; // How fast alpha decreases
        int decayAttempts = 20; // Attempts after which alpha stabilizes

        if (totalAttempts >= decayAttempts) {
            return baseAlpha;
        }

        // Linear decay from maxAlpha to baseAlpha
        double ratio = (double) totalAttempts / decayAttempts;
        return maxAlpha - (maxAlpha - baseAlpha) * ratio;
    }

    /**
     * Calculate performance score based on correctness and difficulty.
     *
     * Correct answers earn more credit for harder questions.
     * Wrong answers lose more credit for easier questions.
     *
     * @param isCorrect  Whether the answer was correct
     * @param difficulty Difficulty level (1-5)
     * @return Performance score (0.0-1.0)
     */
    private double calculatePerformance(boolean isCorrect, DifficultyLevel difficulty) {
        double difficultyWeight = difficulty.getWeight();

        if (isCorrect) {
            // Correct: base 0.7 + bonus for higher difficulty
            // Easy (1.0): 0.7, Medium (1.5): 0.85, Hard (2.0): 1.0
            return 0.7 + (difficultyWeight - 1.0) * 0.3;
        } else {
            // Wrong: base 0.3 - penalty for easier questions
            // Easy (1.0): 0.3, Medium (1.5): 0.15, Hard (2.0): 0.0
            return Math.max(0.0, 0.3 - (2.0 - difficultyWeight) * 0.15);
        }
    }

    /**
     * Calculate mastery delta (change in mastery).
     *
     * @param oldMastery Previous mastery level
     * @param newMastery New mastery level
     * @return Delta value (positive for gain, negative for loss)
     */
    public BigDecimal calculateDelta(BigDecimal oldMastery, BigDecimal newMastery) {
        BigDecimal old = oldMastery != null ? oldMastery : BigDecimal.valueOf(initialMasteryLevel);
        BigDecimal updated = newMastery != null ? newMastery : old;
        return updated.subtract(old).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Convert mastery level to descriptive label.
     *
     * @param mastery Mastery level (0.0-1.0)
     * @return Mastery label (WEAK, DEVELOPING, PROFICIENT, STRONG)
     */
    public MasteryLabel getMasteryLabel(BigDecimal mastery) {
        if (mastery == null) {
            return MasteryLabel.WEAK;
        }

        double value = mastery.doubleValue();
        if (value < 0.4) {
            return MasteryLabel.WEAK;
        } else if (value < 0.6) {
            return MasteryLabel.DEVELOPING;
        } else if (value < 0.8) {
            return MasteryLabel.PROFICIENT;
        } else {
            return MasteryLabel.STRONG;
        }
    }

    /**
     * Calculate target difficulty for next question based on mastery.
     *
     * Maps mastery to appropriate difficulty for Zone of Proximal Development:
     * - Low mastery (< 0.3): Easy questions
     * - Medium mastery (0.3-0.7): Medium questions
     * - High mastery (> 0.7): Hard questions
     *
     * @param mastery Current mastery level
     * @return Recommended difficulty level
     */
    public DifficultyLevel getTargetDifficulty(BigDecimal mastery) {
        if (mastery == null) {
            return DifficultyLevel.EASY;
        }

        double value = mastery.doubleValue();
        if (value < 0.25) {
            return DifficultyLevel.VERY_EASY;
        } else if (value < 0.4) {
            return DifficultyLevel.EASY;
        } else if (value < 0.6) {
            return DifficultyLevel.MEDIUM;
        } else if (value < 0.8) {
            return DifficultyLevel.HARD;
        } else {
            return DifficultyLevel.VERY_HARD;
        }
    }

    /**
     * Get initial mastery level for new skills.
     *
     * @return Initial mastery value
     */
    public BigDecimal getInitialMastery() {
        return BigDecimal.valueOf(initialMasteryLevel).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate weighted average mastery for a category.
     *
     * @param masteries   Array of mastery values
     * @param weights     Array of weights (e.g., question counts)
     * @return Weighted average mastery
     */
    public BigDecimal calculateWeightedAverage(BigDecimal[] masteries, int[] weights) {
        if (masteries == null || weights == null || masteries.length != weights.length || masteries.length == 0) {
            return getInitialMastery();
        }

        double totalWeight = 0;
        double weightedSum = 0;

        for (int i = 0; i < masteries.length; i++) {
            if (masteries[i] != null && weights[i] > 0) {
                weightedSum += masteries[i].doubleValue() * weights[i];
                totalWeight += weights[i];
            }
        }

        if (totalWeight == 0) {
            return getInitialMastery();
        }

        return BigDecimal.valueOf(weightedSum / totalWeight).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate simple average mastery.
     *
     * @param masteries Array of mastery values
     * @return Simple average mastery
     */
    public BigDecimal calculateSimpleAverage(BigDecimal[] masteries) {
        if (masteries == null || masteries.length == 0) {
            return getInitialMastery();
        }

        double sum = 0;
        int count = 0;

        for (BigDecimal mastery : masteries) {
            if (mastery != null) {
                sum += mastery.doubleValue();
                count++;
            }
        }

        if (count == 0) {
            return getInitialMastery();
        }

        return BigDecimal.valueOf(sum / count).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
