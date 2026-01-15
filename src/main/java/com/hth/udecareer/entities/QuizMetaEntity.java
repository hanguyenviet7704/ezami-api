package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "ez_quizmeta")
public class QuizMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_title", nullable = false)
    private String questionTitle;

    @Column(name = "chapter_idx", nullable = false)
    private Integer chapterIdx;

    @Column(name = "quiz_category_id", nullable = false)
    private Long quizCategoryId;
}

