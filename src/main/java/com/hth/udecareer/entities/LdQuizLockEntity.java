package com.hth.udecareer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * LearnDash Pro Quiz Lock
 * Maps to wp_learndash_pro_quiz_lock table
 * Manages quiz access restrictions (IP-based, time-based locks)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_learndash_pro_quiz_lock")
public class LdQuizLockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id")
    private Long id;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "lock_ip")
    private String lockIp;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "lock_date")
    private Long lockDate;
}
