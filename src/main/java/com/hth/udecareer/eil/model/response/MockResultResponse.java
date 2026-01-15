package com.hth.udecareer.eil.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockResultResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("quizId")
    private Long quizId;

    @JsonProperty("certificateCode")
    private String certificateCode;

    @JsonProperty("score")
    private Double score;

    @JsonProperty("totalPoints")
    private Integer totalPoints;

    @JsonProperty("correctCount")
    private Integer correctCount;

    @JsonProperty("totalQuestions")
    private Integer totalQuestions;

    @JsonProperty("timeSpentSeconds")
    private Integer timeSpentSeconds;

    @JsonProperty("percentageScore")
    private Double percentageScore;

    @JsonProperty("passed")
    private Boolean passed;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("answers")
    private List<MockAnswerResponse> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockAnswerResponse {
        @JsonProperty("questionId")
        private Long questionId;

        @JsonProperty("userAnswer")
        private String userAnswer;

        @JsonProperty("correctAnswer")
        private String correctAnswer;

        @JsonProperty("isCorrect")
        private Boolean isCorrect;

        @JsonProperty("pointsEarned")
        private Double pointsEarned;

        @JsonProperty("maxPoints")
        private Double maxPoints;

        /**
         * Explanation for this question (correct_msg or incorrect_msg based on isCorrect).
         * Added to fix frontend truncation warning and provide full explanation in mock test results.
         */
        @JsonProperty("explanation")
        private String explanation;
    }
}
