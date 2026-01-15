package com.hth.udecareer.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.hth.udecareer.entities.QuestionEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ExplainAnswerRequest;
import com.hth.udecareer.model.response.AnswerOptionResponse;
import com.hth.udecareer.model.response.ExplainAnswerResponse;
import com.hth.udecareer.model.response.QuestionResponse;
import com.hth.udecareer.model.response.QuestionResponse.AnswerData;
import com.hth.udecareer.repository.QuestionRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public ExplainAnswerResponse explainAnswer(String email, @NotNull ExplainAnswerRequest request) throws AppException {
        log.info("explainAnswer: user {}, quizId {}, questionId {}",
                email, request.getQuizId(), request.getQuestionId());

        final QuestionEntity questionEntity = questionRepository
                .findById(request.getQuestionId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Question not found"));

        if (!questionEntity.getQuizId().equals(request.getQuizId())) {
            throw new AppException(ErrorCode.INVALID_KEY, "Question does not belong to the specified quiz");
        }

        final QuestionResponse questionResponse = QuestionResponse.from(questionEntity);

        final List<Boolean> userAnswer = request.getAnswerData();
        final List<Boolean> correctAnswer = getCorrectAnswerFromQuestion(questionResponse);
        final List<AnswerOptionResponse> correctAnswerDetails = getCorrectAnswerDetails(questionResponse);

        final boolean isCorrect = isAnswerCorrect(userAnswer, correctAnswer);

        final String explanation = isCorrect
                ? questionResponse.getCorrectMsg()
                : questionResponse.getIncorrectMsg();

        log.info("Explanation retrieved: isCorrect={}, explanationLength={}, explanationPreview={}",
                isCorrect,
                explanation != null ? explanation.length() : 0,
                explanation != null ? explanation.substring(0, Math.min(50, explanation.length())) : "NULL");

        final Integer points = isCorrect
                ? Objects.requireNonNullElse(questionResponse.getPoints(), 0)
                : 0;

        log.info("explainAnswer result: user {}, questionId {}, isCorrect {}, points {}",
                email, request.getQuestionId(), isCorrect, points);

        return ExplainAnswerResponse.builder()
                .isCorrect(isCorrect)
                .correctAnswerDetails(correctAnswerDetails)
                .explanation(explanation)
                .points(points)
                .build();
    }

    private boolean isAnswerCorrect(List<Boolean> userAnswer, List<Boolean> correctAnswer) {
        if (userAnswer == null || correctAnswer == null || userAnswer.size() != correctAnswer.size()) {
            return false;
        }

        for (int i = 0; i < userAnswer.size(); i++) {
            Boolean userChoice = userAnswer.get(i);
            Boolean correctChoice = correctAnswer.get(i);

            // Sử dụng Objects.equals để tránh null pointer và đảm bảo so sánh chính xác
            if (!Objects.equals(userChoice, correctChoice)) {
                return false;
            }
        }

        return true;
    }

    private List<Boolean> getCorrectAnswerFromQuestion(QuestionResponse question) {
        if (question.getAnswerData() == null || question.getAnswerData().isEmpty()) {
            return List.of();
        }

        return question.getAnswerData()
                .stream()
                .sorted(Comparator.comparing(AnswerData::getIndex))
                .map(AnswerData::isCorrect)
                .collect(Collectors.toList());
    }

    private List<AnswerOptionResponse> getCorrectAnswerDetails(QuestionResponse question) {
        if (question.getAnswerData() == null || question.getAnswerData().isEmpty()) {
            return List.of();
        }

        return question.getAnswerData()
                .stream()
                .sorted(Comparator.comparing(AnswerData::getIndex))
                .filter(AnswerData::isCorrect) // Chỉ lấy các đáp án đúng
                .map(answerData -> AnswerOptionResponse.builder()
                        .index(answerData.getIndex())
                        .text(answerData.getAnswer())
                        .build())
                .collect(Collectors.toList());
    }
}
