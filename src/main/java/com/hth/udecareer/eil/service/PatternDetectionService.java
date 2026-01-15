package com.hth.udecareer.eil.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.eil.entities.EilPatternAnalysisEntity;
import com.hth.udecareer.eil.model.request.PatternAnalysisRequest;
import com.hth.udecareer.eil.model.response.PatternAnalysisResponse;
import com.hth.udecareer.eil.repository.EilPatternAnalysisRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for detecting learning patterns from quiz/diagnostic sessions.
 * Detects 6 types of patterns: time_pressure, concept_confusion, careless_mistakes,
 * knowledge_gap, fatigue, overthinking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatternDetectionService {

    private final EilPatternAnalysisRepository patternRepository;
    private final ObjectMapper objectMapper;

    // Pattern detection thresholds
    private static final int FAST_ANSWER_THRESHOLD_SECONDS = 15;
    private static final int SLOW_ANSWER_THRESHOLD_SECONDS = 120;
    private static final double KNOWLEDGE_GAP_ACCURACY_THRESHOLD = 0.50;
    private static final int MIN_QUESTIONS_FOR_KNOWLEDGE_GAP = 2;
    private static final double FATIGUE_ACCURACY_DROP_THRESHOLD = 0.20;
    private static final int MIN_ANSWER_CHANGES_FOR_OVERTHINKING = 2;

    /**
     * Analyze a session and detect learning patterns.
     */
    @Transactional
    public PatternAnalysisResponse analyzeSession(Long userId, PatternAnalysisRequest request) {
        log.info("Analyzing session {} for user {}", request.getSessionId(), userId);

        List<PatternAnalysisRequest.QuestionResult> results = request.getQuestionResults();
        if (results == null || results.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Calculate basic metrics
        int totalQuestions = results.size();
        int correctCount = (int) results.stream().filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count();
        int totalTime = results.stream().mapToInt(r -> r.getTimeSpentSeconds() != null ? r.getTimeSpentSeconds() : 0).sum();
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0;
        int avgTime = totalQuestions > 0 ? totalTime / totalQuestions : 0;

        // Determine time of day
        LocalDateTime sessionStart = request.getSessionStartTime() != null
            ? request.getSessionStartTime()
            : LocalDateTime.now();
        int sessionHour = sessionStart.getHour();
        String timeOfDay = getTimeOfDay(sessionHour);
        int dayOfWeek = sessionStart.getDayOfWeek().getValue();

        // Detect patterns
        List<PatternAnalysisResponse.DetectedPattern> detectedPatterns = new ArrayList<>();
        List<PatternAnalysisResponse.CategoryAnalysis> weakCategories = new ArrayList<>();
        List<PatternAnalysisResponse.CategoryAnalysis> strongCategories = new ArrayList<>();

        // 1. Time pressure pattern
        detectTimePressurePattern(results, detectedPatterns);

        // 2. Knowledge gap pattern (by category)
        Map<String, PatternAnalysisResponse.CategoryAnalysis> categoryAnalysis = analyzeCategoryPerformance(results);
        for (Map.Entry<String, PatternAnalysisResponse.CategoryAnalysis> entry : categoryAnalysis.entrySet()) {
            PatternAnalysisResponse.CategoryAnalysis analysis = entry.getValue();
            if (analysis.getAccuracy().doubleValue() < KNOWLEDGE_GAP_ACCURACY_THRESHOLD
                && analysis.getTotalQuestions() >= MIN_QUESTIONS_FOR_KNOWLEDGE_GAP) {
                detectKnowledgeGapPattern(entry.getKey(), analysis, detectedPatterns);
                weakCategories.add(analysis);
            } else if (analysis.getAccuracy().doubleValue() >= 0.75) {
                strongCategories.add(analysis);
            }
        }

        // 3. Fatigue pattern
        detectFatiguePattern(results, detectedPatterns);

        // 4. Careless mistakes pattern
        detectCarelessMistakesPattern(results, detectedPatterns);

        // 5. Overthinking pattern
        detectOverthinkingPattern(results, detectedPatterns);

        // Calculate fatigue score
        BigDecimal fatigueScore = calculateFatigueScore(results);
        boolean fatigueDetected = fatigueScore.compareTo(new BigDecimal("0.3")) > 0;

        // Determine speed vs accuracy tradeoff
        String speedAccuracyTradeoff = determineSpeedAccuracyTradeoff(avgTime, accuracy);

        // Determine dominant pattern (by highest confidence)
        String dominantPattern = detectedPatterns.isEmpty() ? null :
            detectedPatterns.stream()
                .max(Comparator.comparing(p -> p.getConfidence() != null ? p.getConfidence() : BigDecimal.ZERO))
                .map(PatternAnalysisResponse.DetectedPattern::getType)
                .orElse(null);

        // Generate recommendations
        List<String> recommendations = generateRecommendations(detectedPatterns, categoryAnalysis, accuracy);

        // Calculate confidence
        BigDecimal confidence = calculateConfidence(totalQuestions, detectedPatterns.size());

        // Save to database
        EilPatternAnalysisEntity entity = EilPatternAnalysisEntity.builder()
                .userId(userId)
                .sessionId(request.getSessionId())
                .sessionType(request.getSessionType())
                .certificationCode(request.getCertificationCode())
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .accuracy(BigDecimal.valueOf(accuracy).setScale(4, RoundingMode.HALF_UP))
                .avgTimePerQuestion(avgTime)
                .totalTimeSeconds(totalTime)
                .sessionHour(sessionHour)
                .timeOfDay(timeOfDay)
                .dayOfWeek(dayOfWeek)
                .detectedPatterns(toJson(detectedPatterns))
                .fatigueDetected(fatigueDetected)
                .fatigueScore(fatigueScore)
                .speedAccuracyTradeoff(speedAccuracyTradeoff)
                .weakCategories(toJson(weakCategories))
                .strongCategories(toJson(strongCategories))
                .recommendations(toJson(recommendations))
                .confidence(confidence)
                .sessionStartTime(sessionStart)
                .createdAt(LocalDateTime.now())
                .build();

        entity = patternRepository.save(entity);
        log.info("Pattern analysis saved with ID {} for session {}", entity.getId(), request.getSessionId());

        return buildResponse(entity, detectedPatterns, weakCategories, strongCategories, recommendations);
    }

    /**
     * Get analysis by session ID.
     */
    public PatternAnalysisResponse getBySessionId(Long userId, String sessionId) {
        EilPatternAnalysisEntity entity = patternRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!entity.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return entityToResponse(entity);
    }

    /**
     * Get analysis history for a user.
     */
    public Page<PatternAnalysisResponse> getHistory(Long userId, String categoryCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EilPatternAnalysisEntity> entities;

        if (categoryCode != null && !categoryCode.isEmpty()) {
            entities = patternRepository.findByUserIdAndCertificationCodeOrderByCreatedAtDesc(
                    userId, categoryCode, pageable);
        } else {
            entities = patternRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return entities.map(this::entityToResponse);
    }

    /**
     * Get aggregated stats.
     */
    public PatternAnalysisResponse.PatternStats getStats(Long userId, String categoryCode) {
        List<EilPatternAnalysisEntity> analyses = categoryCode != null && !categoryCode.isEmpty()
            ? patternRepository.findByUserIdAndCertificationCodeOrderByCreatedAtDesc(userId, categoryCode)
            : patternRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (analyses.isEmpty()) {
            return PatternAnalysisResponse.PatternStats.builder()
                    .totalSessions(0L)
                    .totalQuestions(0L)
                    .overallAccuracy(BigDecimal.ZERO)
                    .build();
        }

        long totalQuestions = analyses.stream().mapToLong(a -> a.getTotalQuestions() != null ? a.getTotalQuestions() : 0).sum();
        double avgAccuracy = analyses.stream()
                .filter(a -> a.getAccuracy() != null)
                .mapToDouble(a -> a.getAccuracy().doubleValue())
                .average()
                .orElse(0);

        // Get accuracy by time of day
        Map<String, BigDecimal> accuracyByTimeOfDay = new HashMap<>();
        Map<String, List<Double>> groupedAccuracy = analyses.stream()
                .filter(a -> a.getTimeOfDay() != null && a.getAccuracy() != null)
                .collect(Collectors.groupingBy(
                        EilPatternAnalysisEntity::getTimeOfDay,
                        Collectors.mapping(a -> a.getAccuracy().doubleValue(), Collectors.toList())
                ));
        for (Map.Entry<String, List<Double>> entry : groupedAccuracy.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(d -> d).average().orElse(0);
            accuracyByTimeOfDay.put(entry.getKey(), BigDecimal.valueOf(avg).setScale(4, RoundingMode.HALF_UP));
        }

        String bestTimeOfDay = accuracyByTimeOfDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        long fatigueSessions = analyses.stream().filter(a -> Boolean.TRUE.equals(a.getFatigueDetected())).count();
        double avgFatigueScore = analyses.stream()
                .filter(a -> a.getFatigueScore() != null)
                .mapToDouble(a -> a.getFatigueScore().doubleValue())
                .average()
                .orElse(0);

        return PatternAnalysisResponse.PatternStats.builder()
                .totalSessions((long) analyses.size())
                .totalQuestions(totalQuestions)
                .overallAccuracy(BigDecimal.valueOf(avgAccuracy).setScale(4, RoundingMode.HALF_UP))
                .accuracyByTimeOfDay(accuracyByTimeOfDay)
                .bestTimeOfDay(bestTimeOfDay)
                .fatigueSessions(fatigueSessions)
                .avgFatigueScore(BigDecimal.valueOf(avgFatigueScore).setScale(4, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Delete analysis by session ID.
     */
    @Transactional
    public void deleteBySessionId(Long userId, String sessionId) {
        EilPatternAnalysisEntity entity = patternRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!entity.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        patternRepository.delete(entity);
        log.info("Deleted pattern analysis for session {}", sessionId);
    }

    // ==================== Private Helper Methods ====================

    private void detectTimePressurePattern(List<PatternAnalysisRequest.QuestionResult> results,
                                           List<PatternAnalysisResponse.DetectedPattern> patterns) {
        List<PatternAnalysisRequest.QuestionResult> fastWrong = results.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsCorrect()))
                .filter(r -> r.getTimeSpentSeconds() != null && r.getTimeSpentSeconds() < FAST_ANSWER_THRESHOLD_SECONDS)
                .collect(Collectors.toList());

        if (fastWrong.size() >= 2) {
            List<Long> affectedQuestions = fastWrong.stream()
                    .map(PatternAnalysisRequest.QuestionResult::getQuestionId)
                    .collect(Collectors.toList());

            Set<String> affectedSkills = fastWrong.stream()
                    .map(PatternAnalysisRequest.QuestionResult::getCategory)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            int totalWrong = (int) results.stream().filter(r -> !Boolean.TRUE.equals(r.getIsCorrect())).count();
            double percentage = totalWrong > 0 ? (double) fastWrong.size() / totalWrong * 100 : 0;

            patterns.add(PatternAnalysisResponse.DetectedPattern.builder()
                    .type("time_pressure")
                    .description("You made " + fastWrong.size() + " errors on questions answered in under " + FAST_ANSWER_THRESHOLD_SECONDS + " seconds.")
                    .confidence(BigDecimal.valueOf(0.8))
                    .details(Map.of(
                            "affectedQuestions", affectedQuestions,
                            "affectedSkills", affectedSkills,
                            "frequency", fastWrong.size(),
                            "percentage", percentage
                    ))
                    .build());
        }
    }

    private void detectKnowledgeGapPattern(String category, PatternAnalysisResponse.CategoryAnalysis analysis,
                                           List<PatternAnalysisResponse.DetectedPattern> patterns) {
        double accuracy = analysis.getAccuracy().doubleValue();
        int wrong = analysis.getTotalQuestions() - analysis.getCorrectCount();

        patterns.add(PatternAnalysisResponse.DetectedPattern.builder()
                .type("knowledge_gap")
                .description("You got " + wrong + " out of " + analysis.getTotalQuestions() +
                        " questions wrong in \"" + category + "\" (" + String.format("%.0f", accuracy * 100) + "% accuracy).")
                .confidence(BigDecimal.valueOf(0.9))
                .details(Map.of(
                        "category", category,
                        "accuracy", accuracy,
                        "totalQuestions", analysis.getTotalQuestions(),
                        "correctCount", analysis.getCorrectCount()
                ))
                .build());
    }

    private void detectFatiguePattern(List<PatternAnalysisRequest.QuestionResult> results,
                                      List<PatternAnalysisResponse.DetectedPattern> patterns) {
        if (results.size() < 10) return;

        int half = results.size() / 2;
        List<PatternAnalysisRequest.QuestionResult> firstHalf = results.subList(0, half);
        List<PatternAnalysisRequest.QuestionResult> secondHalf = results.subList(half, results.size());

        double firstAccuracy = firstHalf.stream().filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count() / (double) firstHalf.size();
        double secondAccuracy = secondHalf.stream().filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count() / (double) secondHalf.size();

        double drop = firstAccuracy - secondAccuracy;
        if (drop >= FATIGUE_ACCURACY_DROP_THRESHOLD) {
            patterns.add(PatternAnalysisResponse.DetectedPattern.builder()
                    .type("fatigue")
                    .description("Your accuracy dropped " + String.format("%.0f", drop * 100) +
                            "% in the second half of the session.")
                    .confidence(BigDecimal.valueOf(0.75))
                    .details(Map.of(
                            "firstHalfAccuracy", firstAccuracy,
                            "secondHalfAccuracy", secondAccuracy,
                            "drop", drop
                    ))
                    .build());
        }
    }

    private void detectCarelessMistakesPattern(List<PatternAnalysisRequest.QuestionResult> results,
                                               List<PatternAnalysisResponse.DetectedPattern> patterns) {
        // Would need changedAnswer and initialAnswerWasCorrect fields
        // Simplified version: fast answers that were wrong
        long careless = results.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsCorrect()))
                .filter(r -> r.getTimeSpentSeconds() != null && r.getTimeSpentSeconds() < 20)
                .count();

        if (careless >= 3) {
            patterns.add(PatternAnalysisResponse.DetectedPattern.builder()
                    .type("careless_mistakes")
                    .description("You made " + careless + " quick errors that might be careless mistakes.")
                    .confidence(BigDecimal.valueOf(0.6))
                    .details(Map.of("count", careless))
                    .build());
        }
    }

    private void detectOverthinkingPattern(List<PatternAnalysisRequest.QuestionResult> results,
                                           List<PatternAnalysisResponse.DetectedPattern> patterns) {
        long slowWrong = results.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsCorrect()))
                .filter(r -> r.getTimeSpentSeconds() != null && r.getTimeSpentSeconds() > SLOW_ANSWER_THRESHOLD_SECONDS)
                .count();

        if (slowWrong >= 2) {
            patterns.add(PatternAnalysisResponse.DetectedPattern.builder()
                    .type("overthinking")
                    .description("You spent over " + SLOW_ANSWER_THRESHOLD_SECONDS +
                            " seconds on " + slowWrong + " questions and still got them wrong.")
                    .confidence(BigDecimal.valueOf(0.7))
                    .details(Map.of("count", slowWrong))
                    .build());
        }
    }

    private Map<String, PatternAnalysisResponse.CategoryAnalysis> analyzeCategoryPerformance(
            List<PatternAnalysisRequest.QuestionResult> results) {
        Map<String, List<PatternAnalysisRequest.QuestionResult>> byCategory = results.stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.groupingBy(PatternAnalysisRequest.QuestionResult::getCategory));

        Map<String, PatternAnalysisResponse.CategoryAnalysis> analysis = new HashMap<>();
        for (Map.Entry<String, List<PatternAnalysisRequest.QuestionResult>> entry : byCategory.entrySet()) {
            List<PatternAnalysisRequest.QuestionResult> categoryResults = entry.getValue();
            int total = categoryResults.size();
            int correct = (int) categoryResults.stream().filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count();
            double avgTime = categoryResults.stream()
                    .mapToInt(r -> r.getTimeSpentSeconds() != null ? r.getTimeSpentSeconds() : 0)
                    .average().orElse(0);

            analysis.put(entry.getKey(), PatternAnalysisResponse.CategoryAnalysis.builder()
                    .category(entry.getKey())
                    .totalQuestions(total)
                    .correctCount(correct)
                    .accuracy(BigDecimal.valueOf((double) correct / total).setScale(4, RoundingMode.HALF_UP))
                    .avgTimeSeconds(BigDecimal.valueOf(avgTime).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        return analysis;
    }

    private BigDecimal calculateFatigueScore(List<PatternAnalysisRequest.QuestionResult> results) {
        if (results.size() < 10) return BigDecimal.ZERO;

        int half = results.size() / 2;
        double firstAccuracy = results.subList(0, half).stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count() / (double) half;
        double secondAccuracy = results.subList(half, results.size()).stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count() / (double) (results.size() - half);

        double drop = Math.max(0, firstAccuracy - secondAccuracy);
        return BigDecimal.valueOf(drop).setScale(4, RoundingMode.HALF_UP);
    }

    private String determineSpeedAccuracyTradeoff(int avgTime, double accuracy) {
        if (avgTime < 30 && accuracy < 0.7) return "SPEED_FOCUSED";
        if (avgTime > 90 && accuracy > 0.8) return "ACCURACY_FOCUSED";
        return "BALANCED";
    }

    private List<String> generateRecommendations(List<PatternAnalysisResponse.DetectedPattern> patterns,
                                                  Map<String, PatternAnalysisResponse.CategoryAnalysis> categories,
                                                  double overallAccuracy) {
        List<String> recommendations = new ArrayList<>();

        for (PatternAnalysisResponse.DetectedPattern pattern : patterns) {
            switch (pattern.getType()) {
                case "time_pressure":
                    recommendations.add("Take at least 30 seconds to read and understand each question.");
                    break;
                case "knowledge_gap":
                    recommendations.add("Focus on studying the weak areas identified in this session.");
                    break;
                case "fatigue":
                    recommendations.add("Consider taking breaks during longer study sessions.");
                    break;
                case "careless_mistakes":
                    recommendations.add("Double-check your answers before submitting.");
                    break;
                case "overthinking":
                    recommendations.add("Trust your initial instinct more - don't overthink questions.");
                    break;
            }
        }

        if (overallAccuracy < 0.6) {
            recommendations.add("Review the fundamentals before attempting more practice questions.");
        }

        return recommendations.stream().distinct().collect(Collectors.toList());
    }

    private BigDecimal calculateConfidence(int totalQuestions, int patternCount) {
        double base = Math.min(1.0, totalQuestions / 20.0);
        double patternFactor = patternCount > 0 ? 0.1 : 0;
        return BigDecimal.valueOf(Math.min(0.95, base + patternFactor)).setScale(4, RoundingMode.HALF_UP);
    }

    private String getTimeOfDay(int hour) {
        if (hour >= 5 && hour < 12) return "MORNING";
        if (hour >= 12 && hour < 17) return "AFTERNOON";
        if (hour >= 17 && hour < 21) return "EVENING";
        return "NIGHT";
    }

    private PatternAnalysisResponse buildResponse(EilPatternAnalysisEntity entity,
                                                   List<PatternAnalysisResponse.DetectedPattern> patterns,
                                                   List<PatternAnalysisResponse.CategoryAnalysis> weakCategories,
                                                   List<PatternAnalysisResponse.CategoryAnalysis> strongCategories,
                                                   List<String> recommendations) {
        return PatternAnalysisResponse.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .sessionType(entity.getSessionType())
                .certificationCode(entity.getCertificationCode())
                .totalQuestions(entity.getTotalQuestions())
                .correctCount(entity.getCorrectCount())
                .accuracy(entity.getAccuracy())
                .avgTimePerQuestion(entity.getAvgTimePerQuestion())
                .totalTimeSeconds(entity.getTotalTimeSeconds())
                .sessionHour(entity.getSessionHour())
                .timeOfDay(entity.getTimeOfDay())
                .dayOfWeek(entity.getDayOfWeek())
                .detectedPatterns(patterns)
                .fatigueDetected(entity.getFatigueDetected())
                .fatigueScore(entity.getFatigueScore())
                .speedAccuracyTradeoff(entity.getSpeedAccuracyTradeoff())
                .weakCategories(weakCategories)
                .strongCategories(strongCategories)
                .recommendations(recommendations)
                .confidence(entity.getConfidence())
                .sessionStartTime(entity.getSessionStartTime())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private PatternAnalysisResponse entityToResponse(EilPatternAnalysisEntity entity) {
        List<PatternAnalysisResponse.DetectedPattern> patterns = parseJsonList(entity.getDetectedPatterns(), PatternAnalysisResponse.DetectedPattern.class);
        List<PatternAnalysisResponse.CategoryAnalysis> weakCategories = parseJsonList(entity.getWeakCategories(), PatternAnalysisResponse.CategoryAnalysis.class);
        List<PatternAnalysisResponse.CategoryAnalysis> strongCategories = parseJsonList(entity.getStrongCategories(), PatternAnalysisResponse.CategoryAnalysis.class);
        List<String> recommendations = parseJsonList(entity.getRecommendations(), String.class);

        return buildResponse(entity, patterns, weakCategories, strongCategories, recommendations);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize to JSON", e);
            return null;
        }
    }

    private <T> List<T> parseJsonList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON list", e);
            return new ArrayList<>();
        }
    }
}
