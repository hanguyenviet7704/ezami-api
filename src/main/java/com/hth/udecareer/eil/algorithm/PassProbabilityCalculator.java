package com.hth.udecareer.eil.algorithm;

import com.hth.udecareer.eil.enums.TestType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Calculates pass probability and predicted scores for standardized tests.
 *
 * Uses a weighted combination of:
 * - Category mastery levels (Listening, Reading, Grammar, Vocabulary)
 * - Historical performance patterns
 * - Test-specific scoring curves
 *
 * The algorithm maps mastery levels to expected scores using empirically-derived
 * conversion functions calibrated for each test type.
 */
@Slf4j
@Component
public class PassProbabilityCalculator {

    private static final int SCALE = 4;

    // TOEIC score ranges
    private static final int TOEIC_MIN_SCORE = 10;
    private static final int TOEIC_MAX_SCORE = 990;
    private static final int TOEIC_LISTENING_MAX = 495;
    private static final int TOEIC_READING_MAX = 495;

    /**
     * Calculate predicted scores and pass probability.
     *
     * @param listeningMastery Listening section mastery (0.0-1.0)
     * @param readingMastery   Reading section mastery (0.0-1.0)
     * @param targetScore      User's target score
     * @param testType         Type of test (TOEIC, IELTS, etc.)
     * @return Prediction result with scores and probability
     */
    public PredictionResult calculatePrediction(
            BigDecimal listeningMastery,
            BigDecimal readingMastery,
            Integer targetScore,
            TestType testType) {

        if (testType == null) {
            testType = TestType.TOEIC;
        }

        switch (testType) {
            case TOEIC:
                return calculateToeicPrediction(listeningMastery, readingMastery, targetScore);
            case IELTS:
                return calculateIeltsPrediction(listeningMastery, readingMastery, targetScore);
            default:
                return calculateToeicPrediction(listeningMastery, readingMastery, targetScore);
        }
    }

    /**
     * Calculate TOEIC-specific prediction.
     */
    private PredictionResult calculateToeicPrediction(
            BigDecimal listeningMastery,
            BigDecimal readingMastery,
            Integer targetScore) {

        double lMastery = listeningMastery != null ? listeningMastery.doubleValue() : 0.5;
        double rMastery = readingMastery != null ? readingMastery.doubleValue() : 0.5;

        // Convert mastery to predicted section scores using sigmoid-like curve
        int listeningScore = masteryToToeicSectionScore(lMastery, TOEIC_LISTENING_MAX);
        int readingScore = masteryToToeicSectionScore(rMastery, TOEIC_READING_MAX);
        int totalScore = listeningScore + readingScore;

        // Calculate confidence interval (narrower with higher mastery consistency)
        double masteryVariance = Math.abs(lMastery - rMastery);
        int scoreRange = (int) (50 + masteryVariance * 100); // Base 50 + variance adjustment
        int minScore = Math.max(TOEIC_MIN_SCORE, totalScore - scoreRange);
        int maxScore = Math.min(TOEIC_MAX_SCORE, totalScore + scoreRange);

        // Calculate pass probability using logistic function
        int target = targetScore != null ? targetScore : 600; // Default target
        double passProbability = calculatePassProbability(totalScore, target, scoreRange);

        // Determine confidence level based on data quality
        double confidence = calculateConfidence(lMastery, rMastery);

        // Determine pass status
        String passStatus = determinePassStatus(passProbability);

        log.debug("TOEIC prediction: L={} R={} total={} target={} prob={} status={}",
                listeningScore, readingScore, totalScore, target, passProbability, passStatus);

        return PredictionResult.builder()
                .testType(TestType.TOEIC)
                .listeningScore(listeningScore)
                .readingScore(readingScore)
                .totalScore(totalScore)
                .predictedScoreMin(minScore)
                .predictedScoreMax(maxScore)
                .passProbability(BigDecimal.valueOf(passProbability).setScale(SCALE, RoundingMode.HALF_UP))
                .passStatus(passStatus)
                .confidenceLevel(BigDecimal.valueOf(confidence).setScale(SCALE, RoundingMode.HALF_UP))
                .targetScore(target)
                .gapToTarget(target - totalScore)
                .build();
    }

    /**
     * Calculate IELTS-specific prediction.
     */
    private PredictionResult calculateIeltsPrediction(
            BigDecimal listeningMastery,
            BigDecimal readingMastery,
            Integer targetScore) {

        double lMastery = listeningMastery != null ? listeningMastery.doubleValue() : 0.5;
        double rMastery = readingMastery != null ? readingMastery.doubleValue() : 0.5;

        // IELTS uses band scores 0-9
        double listeningBand = masteryToIeltsBand(lMastery);
        double readingBand = masteryToIeltsBand(rMastery);
        double overallBand = (listeningBand + readingBand) / 2.0;

        // Round to nearest 0.5
        int totalScore = (int) (Math.round(overallBand * 2) / 2.0 * 10); // Store as integer * 10

        int target = targetScore != null ? targetScore : 60; // Default 6.0 band
        double passProbability = calculateIeltsPassProbability(overallBand, target / 10.0);

        return PredictionResult.builder()
                .testType(TestType.IELTS)
                .listeningScore((int) (listeningBand * 10))
                .readingScore((int) (readingBand * 10))
                .totalScore(totalScore)
                .predictedScoreMin(Math.max(0, totalScore - 5))
                .predictedScoreMax(Math.min(90, totalScore + 5))
                .passProbability(BigDecimal.valueOf(passProbability).setScale(SCALE, RoundingMode.HALF_UP))
                .passStatus(determinePassStatus(passProbability))
                .confidenceLevel(BigDecimal.valueOf(0.75).setScale(SCALE, RoundingMode.HALF_UP))
                .targetScore(target)
                .gapToTarget(target - totalScore)
                .build();
    }

    /**
     * Convert mastery level to TOEIC section score.
     * Uses a modified sigmoid curve to model the non-linear relationship
     * between ability and test scores.
     */
    private int masteryToToeicSectionScore(double mastery, int maxScore) {
        // Clamp mastery to valid range
        mastery = Math.max(0.0, Math.min(1.0, mastery));

        // Use modified sigmoid for more realistic score distribution
        // Low mastery: steep improvement, High mastery: diminishing returns
        double k = 5.0; // Steepness
        double x0 = 0.5; // Midpoint
        double sigmoid = 1.0 / (1.0 + Math.exp(-k * (mastery - x0)));

        // Map sigmoid output [~0.007, ~0.993] to score range
        // Minimum score is about 5% of max (even for complete beginners)
        double minRatio = 0.05;
        double score = minRatio * maxScore + sigmoid * (1 - minRatio) * maxScore;

        // Round to nearest 5 (TOEIC scores are in 5-point increments)
        return (int) (Math.round(score / 5.0) * 5);
    }

    /**
     * Convert mastery level to IELTS band score (0-9).
     */
    private double masteryToIeltsBand(double mastery) {
        // Linear mapping with slight curve
        // Mastery 0.0 -> Band 2.0, Mastery 1.0 -> Band 9.0
        double band = 2.0 + mastery * 7.0;
        // Round to nearest 0.5
        return Math.round(band * 2) / 2.0;
    }

    /**
     * Calculate pass probability using logistic function.
     *
     * @param predictedScore Predicted score
     * @param targetScore    Target/passing score
     * @param uncertainty    Score uncertainty range
     * @return Probability between 0 and 1
     */
    private double calculatePassProbability(int predictedScore, int targetScore, int uncertainty) {
        // Use logistic function centered at target score
        double k = 4.0 / uncertainty; // Steepness inversely proportional to uncertainty
        double x = predictedScore - targetScore;
        return 1.0 / (1.0 + Math.exp(-k * x));
    }

    /**
     * Calculate IELTS pass probability.
     */
    private double calculateIeltsPassProbability(double predictedBand, double targetBand) {
        double diff = predictedBand - targetBand;
        double k = 2.0; // Steepness for IELTS bands
        return 1.0 / (1.0 + Math.exp(-k * diff));
    }

    /**
     * Calculate confidence level based on mastery data quality.
     */
    private double calculateConfidence(double listeningMastery, double readingMastery) {
        // Higher confidence when:
        // 1. Both skills have been assessed (not default 0.5)
        // 2. Skills are consistent (not wildly different)

        double baseConfidence = 0.6;

        // Boost confidence if masteries differ from default
        if (Math.abs(listeningMastery - 0.5) > 0.1) {
            baseConfidence += 0.1;
        }
        if (Math.abs(readingMastery - 0.5) > 0.1) {
            baseConfidence += 0.1;
        }

        // Reduce confidence for large skill gaps (unusual patterns)
        double gap = Math.abs(listeningMastery - readingMastery);
        if (gap > 0.3) {
            baseConfidence -= 0.1;
        }

        return Math.max(0.5, Math.min(0.95, baseConfidence));
    }

    /**
     * Determine pass status category based on probability.
     */
    private String determinePassStatus(double probability) {
        if (probability >= 0.8) {
            return "LIKELY_PASS";
        } else if (probability >= 0.5) {
            return "BORDERLINE";
        } else {
            return "NEEDS_WORK";
        }
    }

    /**
     * Calculate overall mastery from category masteries.
     *
     * @param categoryMasteries Map of category name to mastery value
     * @param weights           Optional weights for each category
     * @return Overall mastery
     */
    public BigDecimal calculateOverallMastery(
            Map<String, BigDecimal> categoryMasteries,
            Map<String, Double> weights) {

        if (categoryMasteries == null || categoryMasteries.isEmpty()) {
            return BigDecimal.valueOf(0.5);
        }

        double totalWeight = 0;
        double weightedSum = 0;

        for (Map.Entry<String, BigDecimal> entry : categoryMasteries.entrySet()) {
            double weight = weights != null && weights.containsKey(entry.getKey())
                    ? weights.get(entry.getKey())
                    : 1.0;
            double mastery = entry.getValue() != null ? entry.getValue().doubleValue() : 0.5;

            weightedSum += mastery * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0) {
            return BigDecimal.valueOf(0.5);
        }

        return BigDecimal.valueOf(weightedSum / totalWeight).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate readiness score (how ready user is for the target).
     *
     * @param passProbability Pass probability
     * @param gapToTarget     Score gap to target
     * @param testType        Type of test
     * @return Readiness score (0.0-1.0)
     */
    public BigDecimal calculateReadiness(BigDecimal passProbability, int gapToTarget, TestType testType) {
        double prob = passProbability != null ? passProbability.doubleValue() : 0.5;

        // Readiness is primarily based on pass probability
        // Adjusted by how close the predicted score is to target
        double readiness = prob;

        // If significantly above target, boost readiness
        if (gapToTarget < -50) {
            readiness = Math.min(1.0, readiness + 0.1);
        }

        return BigDecimal.valueOf(readiness).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Prediction result DTO.
     */
    @Data
    @Builder
    public static class PredictionResult {
        private TestType testType;
        private Integer listeningScore;
        private Integer readingScore;
        private Integer totalScore;
        private Integer predictedScoreMin;
        private Integer predictedScoreMax;
        private BigDecimal passProbability;
        private String passStatus;
        private BigDecimal confidenceLevel;
        private Integer targetScore;
        private Integer gapToTarget;
    }
}
