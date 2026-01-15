package com.hth.udecareer.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.math.NumberUtils;

import com.hth.udecareer.model.response.QuestionGroupInfo;
import com.hth.udecareer.model.response.QuestionPartInfo;
import com.hth.udecareer.model.response.QuestionType;
import com.hth.udecareer.model.response.QuestionTypeInfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Entity
@Table(name = "wp_learndash_pro_quiz_question")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "online")
    private Integer online;

    @Column(name = "previous_id")
    private Long previousId;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "title")
    private String title;

    @Column(name = "points")
    private Integer points;

    @Column(name = "question")
    private String question;

    @Column(name = "correct_msg")
    private String correctMsg;

    @Column(name = "incorrect_msg")
    private String incorrectMsg;

    @Column(name = "correct_same_text")
    private Integer correctSameText;

    @Column(name = "tip_enabled")
    private Integer tipEnabled;

    @Column(name = "tip_msg")
    private String tipMsg;

    @Column(name = "answer_type")
    private String answerType;

    @Column(name = "show_points_in_box")
    private Integer showPointsInBox;

    @Column(name = "answer_points_activated")
    private Integer answerPointsActivated;

    @Column(name = "answer_data")
    private String answerData;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "answer_points_diff_modus_activated")
    private Integer answerPointsDiffModusActivated;

    @Column(name = "disable_correct")
    private Integer disableCorrect;

    @Column(name = "matrix_sort_answer_criteria_width")
    private Integer matrixSortAnswerCriteriaWidth;

    @Transient
    public QuestionTypeInfo getQuestionTypeInfo() {
        final String questionTitle = getTitle().strip().toUpperCase();
        final String[] arr = questionTitle.split("_");

        QuestionTypeInfo questionTypeInfo = QuestionTypeInfo
                .builder()
                .type(QuestionType.NORMAL)
                .build();
        try {
            if (arr.length > 3) {
                final String questionGroup = arr[1];
                final int part = NumberUtils.toInt(arr[2].strip(), 0);

                if ("SC".equals(arr[3])) {
                    questionTypeInfo = QuestionPartInfo
                            .builder()
                            .group(questionGroup)
                            .part(part)
                            .type(QuestionType.PART)
                            .build();
                } else if ("G".equals(arr[3])) {
                    questionTypeInfo = QuestionGroupInfo
                            .builder()
                            .group(questionGroup)
                            .part(part)
                            .numberOfQuestions(arr.length > 4 ? NumberUtils.toInt(arr[4].strip(), 0) : 0)
                            .type(QuestionType.GROUP)
                            .build();
                } else {
                    questionTypeInfo = QuestionTypeInfo
                            .builder()
                            .part(part)
                            .group(questionGroup)
                            .type(QuestionType.NORMAL)
                            .build();
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return questionTypeInfo;
    }
}
