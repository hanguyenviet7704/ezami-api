package com.hth.udecareer.entities;

import com.hth.udecareer.enums.DevicePlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity để lưu trữ FCM device tokens của users
 * Mỗi user có thể có nhiều devices (multiple phones, tablets)
 *
 * @author Ezami Team
 * @since 2025-11-27
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ez_user_device_tokens",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_token"}))
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_token", nullable = false, length = 500)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "device_info")
    private String deviceInfo; // Optional: device model, OS version, etc.

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        lastUsedAt = now;
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Update last used timestamp - gọi khi user mở app
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.isActive = true;
    }

    /**
     * Deactivate token - gọi khi user logout hoặc uninstall app
     */
    public void deactivate() {
        this.isActive = false;
    }
}

