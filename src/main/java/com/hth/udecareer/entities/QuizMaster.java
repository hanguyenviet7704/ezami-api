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
@Table(name = "wp_learndash_pro_quiz_master")
public class QuizMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    protected String name;

    @Column(name = "text")
    protected String text;

    @Column(name = "result_text")
    protected String resultText;

    @Column(name = "result_grade_enabled")
    protected Integer resultGradeEnabled;

    @Column(name = "title_hidden")
    protected Integer titleHidden;

    @Column(name = "btn_restart_quiz_hidden")
    protected Integer btnRestartQuizHidden;

    @Column(name = "btn_view_question_hidden")
    protected Integer btnViewQuestionHidden;

    @Column(name = "question_random")
    protected Integer questionRandom;

    @Column(name = "answer_random")
    protected Integer answerRandom;

    @Column(name = "time_limit")
    protected Integer timeLimit;

    @Column(name = "statistics_on")
    protected Integer statisticsOn;

    @Column(name = "statistics_ip_lock")
    protected Integer statisticsIpLock;

    @Column(name = "show_points")
    protected Integer showPoints;

    @Column(name = "numbered_answer")
    protected Integer numberedAnswer;
}
