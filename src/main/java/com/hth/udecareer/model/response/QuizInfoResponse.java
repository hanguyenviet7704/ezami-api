package com.hth.udecareer.model.response;

import static com.hth.udecareer.utils.HtmlUtil.processHtml;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.hth.udecareer.enums.QuizType;
import com.hth.udecareer.model.dto.QuizDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String slug;

    private Integer timeLimit;

    private Long questions;

    private Integer passingPercentage;

    private String name;

    private Long postId;

    private String postContent;

    private String postTitle;

    private Integer showExplain;

    private List<QuestionResponse> questionList;

    private QuizType quizType;

    private Long activityStartTime;

    private Long elapsedTime;

    private Map<Long, List<Integer>> savedAnswers;

    // Các trường thống kê từ completed activity (bài thi đã nộp)
    private Long answeredQuestions;
    private Double percentage;
    private Long answeredCorrects;
    private Long answeredScore;
    private Long pass;

    public static QuizInfoResponse from(final QuizDto quizDto,
                                        final List<QuestionResponse> questionResponseList,
                                        final Map<Long, List<Integer>> savedAnswers,
                                        final Long activityStartTime,
                                        final Long elapsedTime,
                                        final Long answeredQuestions,
                                        final Double percentage,
                                        final Long answeredCorrects,
                                        final Long answeredScore,
                                        final Long pass) {
        return builder()
                .id(quizDto.getId())
                .slug(quizDto.getSlug())
                .timeLimit(quizDto.getTimeLimit())
                .name(quizDto.getName())
                .postId(quizDto.getPostId())
                .postContent(processHtml(quizDto.getPostContent()))
                .postTitle(quizDto.getPostTitle())
                .questionList(questionResponseList)
                .showExplain(1)
                .quizType(quizDto.getQuizType())
                .savedAnswers(savedAnswers)
                .activityStartTime(activityStartTime)
                .elapsedTime(elapsedTime)
                .answeredQuestions(answeredQuestions)
                .percentage(percentage)
                .answeredCorrects(answeredCorrects)
                .answeredScore(answeredScore)
                .pass(pass)
                .build();
    }

    // Overload method để backward compatible
    public static QuizInfoResponse from(final QuizDto quizDto,
                                        final List<QuestionResponse> questionResponseList,
                                        final Map<Long, List<Integer>> savedAnswers,
                                        final Long activityStartTime,
                                        final Long elapsedTime) {
        return from(quizDto, questionResponseList, savedAnswers, activityStartTime, elapsedTime,
                    null, null, null, null, null);
    }

    public static QuizInfoResponse from(final QuizDto quizDto,
                                        final List<QuestionResponse> questionResponseList,
                                        final Map<Long, List<Integer>> savedAnswers,
                                        final Long activityStartTime) {
        return from(quizDto, questionResponseList, savedAnswers, activityStartTime, null);
    }

    public static QuizInfoResponse from(final QuizDto quizDto) {
        return builder()
                .id(quizDto.getId())
                .slug(quizDto.getSlug())
                .timeLimit(quizDto.getTimeLimit())
                .name(quizDto.getName())
                .postId(quizDto.getPostId())
                .postContent(processHtml(quizDto.getPostContent()))
                .postTitle(quizDto.getPostTitle())
                .questionList(List.of())
                .showExplain(1)
                .quizType(quizDto.getQuizType())
                .build();
    }
}
