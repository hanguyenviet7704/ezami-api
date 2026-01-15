package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.entities.MockResultAnswerEntity;
import com.hth.udecareer.eil.entities.MockResultEntity;
import com.hth.udecareer.eil.model.request.SaveMockResultRequest;
import com.hth.udecareer.eil.model.response.MockResultResponse;
import com.hth.udecareer.eil.model.response.MockResultStatsResponse;
import com.hth.udecareer.eil.repository.MockResultRepository;
import com.hth.udecareer.entities.QuestionEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockResultService {

    private final MockResultRepository mockResultRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public MockResultResponse saveMockResult(Long userId, SaveMockResultRequest request) {
        log.info("Saving mock result for user {} - quiz: {}, cert: {}", 
                 userId, request.getQuizId(), request.getCertificateCode());

        // Create mock result entity
        MockResultEntity mockResult = MockResultEntity.builder()
                .userId(userId)
                .quizId(request.getQuizId())
                .certificateCode(request.getCertificateCode())
                .score(request.getScore())
                .totalPoints(request.getTotalPoints())
                .correctCount(request.getCorrectCount())
                .totalQuestions(request.getTotalQuestions())
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .answers(new ArrayList<>())
                .build();

        // Add answer details
        if (request.getAnswers() != null) {
            List<MockResultAnswerEntity> answers = request.getAnswers().stream()
                    .map(ans -> MockResultAnswerEntity.builder()
                            .mockResult(mockResult)
                            .questionId(ans.getQuestionId())
                            .userAnswer(ans.getUserAnswer())
                            .correctAnswer(ans.getCorrectAnswer())
                            .isCorrect(ans.getIsCorrect())
                            .pointsEarned(ans.getPointsEarned())
                            .maxPoints(ans.getMaxPoints())
                            .build())
                    .collect(Collectors.toList());
            mockResult.setAnswers(answers);
        }

        MockResultEntity saved = mockResultRepository.save(mockResult);
        log.info("Mock result saved with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public MockResultResponse getLatestResult(Long userId, String certificateCode) {
        MockResultEntity result;
        if (certificateCode != null && !certificateCode.isEmpty()) {
            result = mockResultRepository
                    .findFirstByUserIdAndCertificateCodeOrderByCreatedAtDesc(userId, certificateCode)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                                                         "No mock result found"));
        } else {
            result = mockResultRepository
                    .findFirstByUserIdOrderByCreatedAtDesc(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                                                         "No mock result found"));
        }
        return mapToResponse(result);
    }

    @Transactional(readOnly = true)
    public Page<MockResultResponse> getResultHistory(Long userId, String certificateCode, 
                                                      int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MockResultEntity> results;
        
        if (certificateCode != null && !certificateCode.isEmpty()) {
            results = mockResultRepository
                    .findByUserIdAndCertificateCodeOrderByCreatedAtDesc(userId, certificateCode, pageable);
        } else {
            results = mockResultRepository
                    .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return results.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MockResultResponse getResultById(Long userId, Long resultId) {
        MockResultEntity result = mockResultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, 
                                                     "Mock result not found"));
        
        // Verify ownership
        if (!result.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Access denied");
        }
        
        return mapToResponse(result);
    }

    @Transactional(readOnly = true)
    public MockResultStatsResponse getStats(Long userId, String certificateCode) {
        Double maxScore = mockResultRepository.findMaxScoreByUserIdAndCertificateCode(userId, certificateCode);
        Double avgScore = mockResultRepository.findAvgScoreByUserIdAndCertificateCode(userId, certificateCode);
        Long totalAttempts = mockResultRepository.countByUserIdAndCertificateCode(userId, certificateCode);
        
        return MockResultStatsResponse.builder()
                .certificateCode(certificateCode)
                .maxScore(maxScore != null ? maxScore : 0.0)
                .avgScore(avgScore != null ? avgScore : 0.0)
                .totalAttempts(totalAttempts != null ? totalAttempts : 0L)
                .passedAttempts(0L) // TODO: calculate based on passed flag
                .build();
    }

    private MockResultResponse mapToResponse(MockResultEntity entity) {
        // Fetch questions to get explanations (correct_msg/incorrect_msg)
        // Group by questionId for efficient lookup
        List<Long> questionIds = entity.getAnswers().stream()
                .map(MockResultAnswerEntity::getQuestionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, QuestionEntity> questionsMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            List<QuestionEntity> questions = questionRepository.findAllById(questionIds);
            questionsMap = questions.stream()
                    .collect(Collectors.toMap(QuestionEntity::getId, q -> q));
        }

        // Build answer responses with explanations
        final Map<Long, QuestionEntity> finalQuestionsMap = questionsMap;
        List<MockResultResponse.MockAnswerResponse> answers = entity.getAnswers().stream()
                .map(ans -> {
                    QuestionEntity question = finalQuestionsMap.get(ans.getQuestionId());
                    String explanation = null;

                    if (question != null) {
                        // Select appropriate explanation based on correctness
                        explanation = Boolean.TRUE.equals(ans.getIsCorrect())
                                ? question.getCorrectMsg()
                                : question.getIncorrectMsg();
                    }

                    return MockResultResponse.MockAnswerResponse.builder()
                            .questionId(ans.getQuestionId())
                            .userAnswer(ans.getUserAnswer())
                            .correctAnswer(ans.getCorrectAnswer())
                            .isCorrect(ans.getIsCorrect())
                            .pointsEarned(ans.getPointsEarned())
                            .maxPoints(ans.getMaxPoints())
                            .explanation(explanation)  // ✅ Now includes explanation!
                            .build();
                })
                .collect(Collectors.toList());

        return MockResultResponse.builder()
                .id(entity.getId())
                .quizId(entity.getQuizId())
                .certificateCode(entity.getCertificateCode())
                .score(entity.getScore())
                .totalPoints(entity.getTotalPoints())
                .correctCount(entity.getCorrectCount())
                .totalQuestions(entity.getTotalQuestions())
                .timeSpentSeconds(entity.getTimeSpentSeconds())
                .percentageScore(entity.getPercentageScore())
                .passed(entity.getPassed())
                .createdAt(entity.getCreatedAt())
                .answers(answers)
                .build();
    }
}
