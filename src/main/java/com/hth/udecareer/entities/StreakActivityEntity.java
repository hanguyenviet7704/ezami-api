package com.hth.udecareer.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hth.udecareer.config.TimezoneConfig;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_streak_activities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_date"}))
public class StreakActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "activity_type", length = 50)
    private String activityType;

    @Column(name = "posts_count")
    @Builder.Default
    private Integer postsCount = 0;

    @Column(name = "comments_count")
    @Builder.Default
    private Integer commentsCount = 0;

    @Column(name = "reactions_count")
    @Builder.Default
    private Integer reactionsCount = 0;

    @Column(name = "quizzes_completed")
    @Builder.Default
    private Integer quizzesCompleted = 0;

    @Column(name = "lessons_completed")
    @Builder.Default
    private Integer lessonsCompleted = 0;

    @Column(name = "streak_day")
    private Integer streakDay;

    @Column(name = "is_freeze_used")
    @Builder.Default
    private Boolean isFreezeUsed = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimezoneConfig.getCurrentVietnamTime();
    }
}
