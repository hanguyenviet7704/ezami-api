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
@Table(name = "wp_fcom_user_streak_goals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "goal_id", "completed_at"}))
public class UserStreakGoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, CLAIMED, EXPIRED

    @Column(name = "progress_json", columnDefinition = "JSON")
    private String progressJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CLAIMED = "CLAIMED";
    public static final String STATUS_EXPIRED = "EXPIRED";

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
