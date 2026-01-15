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
@Table(name = "wp_fcom_posts")
public class FeedPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_rendered", columnDefinition = "TEXT")
    private String messageRendered;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "privacy", length = 20)
    private String privacy;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "featured_image")
    private String featuredImage;

    @Column(name = "comments_count")
    @Builder.Default
    private Integer commentsCount = 0;

    @Column(name = "reactions_count")
    @Builder.Default
    private Integer reactionsCount = 0;

    @Column(name = "is_sticky")
    @Builder.Default
    private Integer isSticky = 0;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "meta", columnDefinition = "JSON")
    private String meta;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

