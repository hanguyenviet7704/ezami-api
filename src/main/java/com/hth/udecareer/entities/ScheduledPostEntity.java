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
@Table(name = "wp_fcom_scheduled_posts")
public class ScheduledPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "title")
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "media", columnDefinition = "JSON")
    private String media; // JSON array of media URLs

    @Column(name = "topic_ids", columnDefinition = "JSON")
    private String topicIds; // JSON array of topic IDs

    @Column(name = "privacy", length = 20)
    @Builder.Default
    private String privacy = "public"; // 'public', 'followers', 'private'

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "scheduled"; // 'scheduled', 'published', 'cancelled', 'failed'

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_post_id")
    private Long publishedPostId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Status constants
    public static final String STATUS_SCHEDULED = "scheduled";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_FAILED = "failed";

    // Privacy constants
    public static final String PRIVACY_PUBLIC = "public";
    public static final String PRIVACY_FOLLOWERS = "followers";
    public static final String PRIVACY_PRIVATE = "private";

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
