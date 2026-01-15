package com.hth.udecareer.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for follow/block relationships between users.
 * Maps to wp_fcom_followers table.
 *
 * Level values:
 * - 0: Blocked
 * - 1: Following (without notifications)
 * - 2: Following with notifications enabled
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_followers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"}))
public class FollowEntity {

    public static final int LEVEL_BLOCKED = 0;
    public static final int LEVEL_FOLLOWING = 1;
    public static final int LEVEL_FOLLOWING_WITH_NOTIFICATIONS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followed_id", nullable = false)
    private Long followedId;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = LEVEL_FOLLOWING;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Convenience methods
    public boolean isBlocked() {
        return level != null && level == LEVEL_BLOCKED;
    }

    public boolean isFollowing() {
        return level != null && level >= LEVEL_FOLLOWING;
    }

    public boolean hasNotificationsEnabled() {
        return level != null && level == LEVEL_FOLLOWING_WITH_NOTIFICATIONS;
    }
}
