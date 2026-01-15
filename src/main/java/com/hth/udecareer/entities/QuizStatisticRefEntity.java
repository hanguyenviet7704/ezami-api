package com.hth.udecareer.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "wp_learndash_pro_quiz_statistic_ref")
public class QuizStatisticRefEntity {
    @Id
    @Column(name = "statistic_ref_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "quiz_post_id")
    private Long quizPostId;

    @Column(name = "course_post_id")
    private Long coursePostId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "is_old")
    private Integer isOld;

    @Column(name = "form_data")
    private Integer formData;
}
