package com.hth.udecareer.model.response;

import com.hth.udecareer.enums.QuizType;
import com.hth.udecareer.model.dto.PointInfoDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitAnswerResponse {
    private Long activityId;

    private Long point;

    private Long totalPoint;

    private double percentage;

    private int passPercentage;

    private boolean pass;

    private Long corrects;

    private Long questions;

    private Long answeredQuestions;

    private Long inCorrects;

    private Long answeredTime;

    private QuizType quizType;

    private PointInfoDto pointInfo;
}
