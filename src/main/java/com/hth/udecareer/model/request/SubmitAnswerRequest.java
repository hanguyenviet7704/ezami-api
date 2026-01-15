package com.hth.udecareer.model.request;

import java.util.List;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Long startTime;

    private Long endTime;

    private List<AnsweredData> data;

    private Long elapsedTime;

    @Data
    public static class AnsweredData {
        private Long questionId;

        private List<Boolean> answerData;
    }

    private Boolean isDraft;
}
