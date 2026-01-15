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
@Table(name = "wp_learndash_user_activity")
public class UserActivityEntity {
    @Id
    @Column(name = "activity_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "activity_type")
    private String activityType;

    @Column(name = "activity_status")
    private Integer activityStatus;

    @Column(name = "activity_started")
    private Long activityStarted;

    @Column(name = "activity_completed")
    private Long activityCompleted;

    @Column(name = "activity_updated")
    private Long activityUpdated;
}
