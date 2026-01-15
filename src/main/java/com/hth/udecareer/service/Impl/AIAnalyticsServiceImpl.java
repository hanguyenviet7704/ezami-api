package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.QuizMetaEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.request.AIAnalyticsRequest;
import com.hth.udecareer.model.dto.response.AIAnalyticsResponse;
import com.hth.udecareer.model.response.QuizHistoryResponse;
import com.hth.udecareer.model.response.QuizHistoryWrapperResponse;
import com.hth.udecareer.model.response.QuizResultDetailResponse;
import com.hth.udecareer.repository.QuizMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.AIAnalyticsService;
import com.hth.udecareer.service.QuizMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalyticsServiceImpl implements AIAnalyticsService {

    private static final String AI_BASE_URL = "http://192.168.11.156:8000";
    private static final String AI_WEAKNESS_PATH = "/ai/weakness-analysis";

    private final UserRepository userRepository;
    private final QuizMasterService quizMasterService;
    private final QuizMetaRepository quizMetaRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public AIAnalyticsResponse getStudentStats(Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        QuizHistoryWrapperResponse quizHistoryWithStats = quizMasterService.getQuizHistoryWithStats(principal.getName(), null, null,
                null, null, "time", "desc", 0,1000);

        List<QuizHistoryResponse> historyList = quizHistoryWithStats.getHistory().getContent();
        log.info("AI analytics - history size: {}", historyList.size());

        List<Long> activityIds = historyList.stream()
                .map(QuizHistoryResponse::getActivityId)
                .filter(Objects::nonNull)
                .toList();

        log.info("AI analytics - activity count: {}", activityIds.size());

        // Lấy chi tiết từng activity
        List<QuizResultDetailResponse> activityDetails = new ArrayList<>();
        for (Long activityId : activityIds) {
            activityDetails.add(quizMasterService.getQuizResultDetail(principal.getName(), activityId));
        }

        // Thu thập toàn bộ questionTitle để map chapter
        Set<String> questionTitles = activityDetails.stream()
                .flatMap(detail -> detail.getQuestions().stream())
                .map(QuizResultDetailResponse.QuestionDetail::getQuestionTitle)
                .filter(StringUtils::isNotBlank)
                .map(title -> StringUtils.upperCase(title.strip()))
                .collect(Collectors.toCollection(HashSet::new));
        log.info("AI analytics - unique question titles (normalized): {}", questionTitles.size());

        Map<String, QuizMetaEntity> questionTitleToMeta = questionTitles.isEmpty()
                ? Map.of()
                : quizMetaRepository.findAllByQuestionTitleIn(questionTitles)
                .stream()
                .collect(Collectors.toMap(
                        qm -> StringUtils.upperCase(qm.getQuestionTitle().strip()),
                        Function.identity(),
                        (a, b) -> a));
        log.info("AI analytics - questions mapped to chapter: {}", questionTitleToMeta.size());

        Map<Integer, ChapterAccumulator> chapterMap = new HashMap<>();
        int totalQuestionsProcessed = 0;
        int matchedQuestions = 0;
        List<String> unmatchedSamples = new ArrayList<>();

        for (QuizResultDetailResponse detail : activityDetails) {
            if (detail.getQuestions() == null) {
                continue;
            }
            for (QuizResultDetailResponse.QuestionDetail question : detail.getQuestions()) {
                totalQuestionsProcessed++;
                String title = StringUtils.upperCase(StringUtils.strip(question.getQuestionTitle()));
                if (StringUtils.isBlank(title)) {
                    continue;
                }
                QuizMetaEntity meta = questionTitleToMeta.get(title);
                if (meta == null || meta.getChapterIdx() == null) {
                    if (unmatchedSamples.size() < 5) {
                        unmatchedSamples.add(title);
                    }
                    continue;
                }

                matchedQuestions++;
                ChapterAccumulator acc = chapterMap.computeIfAbsent(meta.getChapterIdx(), c -> new ChapterAccumulator());
                acc.total++;
                if (Boolean.TRUE.equals(question.getIsCorrect())) {
                    acc.correct++;
                }
            }
        }
        log.info("AI analytics - questions processed: {}, matched: {}", totalQuestionsProcessed, matchedQuestions);
        if (!unmatchedSamples.isEmpty()) {
            log.info("AI analytics - sample unmatched titles (normalized): {}", unmatchedSamples);
        }

        List<AIAnalyticsRequest.ChapterStat> chapterStats = chapterMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    int chapterIdx = entry.getKey();
                    ChapterAccumulator acc = entry.getValue();
                    double accuracy = acc.total == 0 ? 0D : (acc.correct * 100.0) / acc.total;
                    return AIAnalyticsRequest.ChapterStat.builder()
                            .chapter(String.valueOf(chapterIdx))
                            .accuracy(accuracy)
                            .status(determineStatus(accuracy))
                            .build();
                })
                .toList();

        // Calculate readiness score based on accuracy and practice volume
        double overallAccuracy = quizHistoryWithStats.getStatistics().getAverageScore();
        Long totalAttempts = quizHistoryWithStats.getStatistics().getTotalAttempts();
        int readinessScore = calculateReadinessScore(overallAccuracy, totalAttempts != null ? totalAttempts.intValue() : 0);

        AIAnalyticsRequest request = AIAnalyticsRequest.builder()
                .userId(user.getId())
                .totalAttempts(totalAttempts)
                .overallAccuracy(overallAccuracy)
                .readinessScore(readinessScore)
                .chapterStats(chapterStats)
                .build();

        log.info("AI analytics - request: {}", request);
        log.info("AI analytics - chapterStats size: {}", chapterStats.size());

        // Call AI service
        WebClient client = webClientBuilder
                .baseUrl(AI_BASE_URL)
                .build();

        try {
            return client.post()
                    .uri(AI_WEAKNESS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AIAnalyticsResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("AI analytics - call failed status {} body {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }

    private String determineStatus(double accuracy) {
        if (accuracy >= 75) {
            return "GOOD";
        }
        if (accuracy >= 60) {
            return "MODERATE";
        }
        return "WEAK";
    }

    /**
     * Calculate readiness score based on quiz performance.
     * Formula: 70% accuracy weight + 30% practice volume weight
     *
     * @param overallAccuracy Average quiz accuracy (0-100)
     * @param totalAttempts Number of quiz attempts
     * @return Readiness score (0-100)
     */
    private int calculateReadinessScore(double overallAccuracy, int totalAttempts) {
        // Accuracy component: 70% weight
        double accuracyComponent = overallAccuracy * 0.7;

        // Practice volume component: 30% weight, capped at 30 attempts
        int cappedAttempts = Math.min(totalAttempts, 30);
        double volumeComponent = (cappedAttempts / 30.0) * 30;

        return (int) Math.round(accuracyComponent + volumeComponent);
    }

    private static class ChapterAccumulator {
        int total = 0;
        int correct = 0;
    }
}
