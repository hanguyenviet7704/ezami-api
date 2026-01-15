package com.hth.udecareer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * LearnDash Pro Quiz Prerequisite
 * Maps to wp_learndash_pro_quiz_prerequisite table
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_learndash_pro_quiz_prerequisite")
public class LdQuizPrerequisiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prerequisite_id")
    private Long id;

    @Column(name = "prerequisite_quiz_id")
    private Long prerequisiteQuizId;

    @Column(name = "quiz_id")
    private Long quizId;
}
