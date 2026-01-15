package com.hth.udecareer.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hth.udecareer.config.TimezoneConfig;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_streak_goals")
public class StreakGoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_type", nullable = false, length = 30)
    private String goalType; // STREAK_MILESTONE, DAILY_TARGET, TIMED_CHALLENGE

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 255)
    private String icon;

    @Column(name = "requirement_json", nullable = false, columnDefinition = "JSON")
    private String requirementJson;

    @Column(name = "reward_json", nullable = false, columnDefinition = "JSON")
    private String rewardJson;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_repeatable")
    @Builder.Default
    private Boolean isRepeatable = false;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Goal type constants
    public static final String TYPE_STREAK_MILESTONE = "STREAK_MILESTONE";
    public static final String TYPE_DAILY_TARGET = "DAILY_TARGET";
    public static final String TYPE_TIMED_CHALLENGE = "TIMED_CHALLENGE";

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = TimezoneConfig.getCurrentVietnamTime();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = TimezoneConfig.getCurrentVietnamTime();
    }
}
