package com.hth.udecareer.model.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExplainAnswerResponse {
    private Boolean isCorrect;
    private List<AnswerOptionResponse> correctAnswerDetails;
    private String explanation;
    private Integer points;
}
