package com.hth.udecareer.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hth.udecareer.config.TimezoneConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_notification_users")
public class NotificationUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "is_read")
    @Builder.Default
    private Integer isRead = 0;

    @Column(name = "object_type", length = 50)
    @Builder.Default
    private String objectType = "notification";

    @Column(name = "notification_type", length = 50)
    @Builder.Default
    private String notificationType = "web";

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = TimezoneConfig.getCurrentVietnamTime();
        createdAt = now;
        updatedAt = now;
        if (isRead == null) {
            isRead = 0;
        }
        if (objectType == null) {
            objectType = "notification";
        }
        if (notificationType == null) {
            notificationType = "web";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = TimezoneConfig.getCurrentVietnamTime();
    }


    public boolean isNotificationRead() {
        return isRead != null && isRead == 1;
    }

    public void markAsRead() {
        this.isRead = 1;
    }

    public void markAsUnread() {
        this.isRead = 0;
    }

    public boolean getIsRead() {
        return isNotificationRead();
    }

    public void setIsRead(boolean read) {
        this.isRead = read ? 1 : 0;
    }
}
