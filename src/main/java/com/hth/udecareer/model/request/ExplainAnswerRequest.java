package com.hth.udecareer.model.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ExplainAnswerRequest {
    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Answer data is required")
    private List<Boolean> answerData;
}
