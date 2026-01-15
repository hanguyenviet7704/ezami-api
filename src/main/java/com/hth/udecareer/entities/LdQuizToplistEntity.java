package com.hth.udecareer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * LearnDash Pro Quiz Toplist (Leaderboard)
 * Maps to wp_learndash_pro_quiz_toplist table
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_learndash_pro_quiz_toplist")
public class LdQuizToplistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "toplist_id")
    private Long id;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "date")
    private Long date;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "points")
    private Integer points;

    @Column(name = "result")
    private Double result;

    @Column(name = "ip")
    private String ip;
}
