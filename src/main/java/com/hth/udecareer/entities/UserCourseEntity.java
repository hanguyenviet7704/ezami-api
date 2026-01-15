package com.hth.udecareer.entities;

import javax.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "wp_stm_lms_user_courses")
public class UserCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_course_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "current_lesson_id")
    private Long currentLessonId;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent;

    @Column(name = "final_grade")
    private Integer finalGrade;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "lng_code", nullable = false)
    private String lngCode;

    @Column(name = "is_gradable", nullable = false)
    private Boolean isGradable;

    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @Column(name = "enterprise_id")
    private Integer enterpriseId;

    @Column(name = "bundle_id")
    private Integer bundleId;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "end_time")
    private Long endTime;

    @Column(name = "for_points")
    private String forPoints;

}
