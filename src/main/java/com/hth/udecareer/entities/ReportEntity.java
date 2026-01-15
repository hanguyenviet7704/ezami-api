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
@Table(name = "wp_fcom_reports")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reported_user_id")
    private Long reportedUserId;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "object_type", length = 50)
    private String objectType; // 'feed', 'comment', 'user', 'space'

    @Column(name = "reason", length = 100)
    private String reason; // 'spam', 'harassment', 'hate_speech', 'violence', 'inappropriate', 'other'

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "pending"; // 'pending', 'reviewing', 'resolved', 'dismissed'

    @Column(name = "moderator_id")
    private Long moderatorId;

    @Column(name = "moderator_notes", columnDefinition = "TEXT")
    private String moderatorNotes;

    @Column(name = "action_taken", length = 50)
    private String actionTaken; // 'none', 'content_removed', 'user_warned', 'user_banned'

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constants for object types
    public static final String OBJECT_TYPE_FEED = "feed";
    public static final String OBJECT_TYPE_COMMENT = "comment";
    public static final String OBJECT_TYPE_USER = "user";
    public static final String OBJECT_TYPE_SPACE = "space";

    // Constants for reasons
    public static final String REASON_SPAM = "spam";
    public static final String REASON_HARASSMENT = "harassment";
    public static final String REASON_HATE_SPEECH = "hate_speech";
    public static final String REASON_VIOLENCE = "violence";
    public static final String REASON_INAPPROPRIATE = "inappropriate";
    public static final String REASON_OTHER = "other";

    // Constants for status
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REVIEWING = "reviewing";
    public static final String STATUS_RESOLVED = "resolved";
    public static final String STATUS_DISMISSED = "dismissed";

    // Constants for actions
    public static final String ACTION_NONE = "none";
    public static final String ACTION_CONTENT_REMOVED = "content_removed";
    public static final String ACTION_USER_WARNED = "user_warned";
    public static final String ACTION_USER_BANNED = "user_banned";

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
