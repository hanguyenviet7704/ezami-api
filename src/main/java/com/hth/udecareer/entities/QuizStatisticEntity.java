package com.hth.udecareer.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;

@Data
@Entity
@Table(name = "wp_learndash_pro_quiz_statistic")
public class QuizStatisticEntity {
    @EmbeddedId
    private QuizStatisticId id;

    @Column(name = "question_post_id")
    private Long questionPostId;

    @Column(name = "correct_count")
    private Long correctCount;

    @Column(name = "incorrect_count")
    private Long incorrectCount;

    @Column(name = "hint_count")
    private Long hintCount;

    @Column(name = "points")
    private Long points;

    @Column(name = "question_time")
    private Long questionTime;

    @Column(name = "answer_data")
    private String answerData;

    @Transient
    private String group;

    @Transient
    private Long questionPoint;
}
