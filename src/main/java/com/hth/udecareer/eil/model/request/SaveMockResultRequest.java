package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveMockResultRequest {

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

    @JsonProperty("answers")
    private List<MockAnswerDetail> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockAnswerDetail {
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
    }
}
