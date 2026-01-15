package com.hth.udecareer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * LearnDash Pro Quiz Form
 * Maps to wp_learndash_pro_quiz_form table
 * Stores custom form fields for quiz registration/submission
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_learndash_pro_quiz_form")
public class LdQuizFormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "form_id")
    private Long id;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "fieldname")
    private String fieldName;

    @Column(name = "fieldtext")
    private String fieldText;

    @Column(name = "type")
    private Integer type;

    @Column(name = "required")
    private Integer required;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data;
}
