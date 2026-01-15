package com.hth.udecareer.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ez_support_log")
public class SupportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String title;

    @Column(columnDefinition = "mediumtext")
    private String description;

    @Column(name = "image_urls", columnDefinition = "json")
    private String imageUrls;

    @Column(name = "device_info", columnDefinition = "json")
    private String deviceInfo;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel;

    @Column(nullable = false)
    private String status;  // Map với Enum SupportStatus

    @Column(name = "admin_note", columnDefinition = "mediumtext")
    private String adminNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
