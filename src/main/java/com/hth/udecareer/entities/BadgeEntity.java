package com.hth.udecareer.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hth.udecareer.config.TimezoneConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_badges")
public class BadgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 255)
    private String icon; // URL to badge icon

    @Column(name = "color", length = 20)
    private String color; // Badge color code

    @Column(name = "type", length = 50)
    @Builder.Default
    private String type = "achievement"; // 'achievement', 'milestone', 'special', 'custom'

    @Column(name = "requirement_type", length = 50)
    private String requirementType; // 'posts', 'comments', 'reactions', 'followers', 'points', 'manual'

    @Column(name = "requirement_value")
    private Integer requirementValue; // Value needed to earn badge

    @Column(name = "points_reward")
    @Builder.Default
    private Integer pointsReward = 0; // Points given when badge is earned

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0; // Display order

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Badge types
    public static final String TYPE_ACHIEVEMENT = "achievement";
    public static final String TYPE_MILESTONE = "milestone";
    public static final String TYPE_SPECIAL = "special";
    public static final String TYPE_CUSTOM = "custom";

    // Requirement types
    public static final String REQ_POSTS = "posts";
    public static final String REQ_COMMENTS = "comments";
    public static final String REQ_REACTIONS = "reactions";
    public static final String REQ_FOLLOWERS = "followers";
    public static final String REQ_POINTS = "points";
    public static final String REQ_MANUAL = "manual";
    // Certificate-based requirement types
    public static final String REQ_COURSE_CERTIFICATES = "course_certificates";
    public static final String REQ_QUIZ_CERTIFICATES = "quiz_certificates";
    public static final String REQ_TOTAL_CERTIFICATES = "total_certificates";
    // Streak-based requirement types
    public static final String REQ_STREAK = "streak";
    public static final String REQ_STREAK_FREEZE = "streak_freeze";

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
