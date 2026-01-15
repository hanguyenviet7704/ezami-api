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
@Table(name = "wp_fcom_spaces")
public class SpaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String logo;

    @Column(name = "cover_photo", columnDefinition = "TEXT")
    private String coverPhoto;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String type;

    @Column(length = 100)
    @Builder.Default
    private String privacy = "public";

    @Column(length = 100)
    @Builder.Default
    private String status = "published";

    @Column
    @Builder.Default
    private Integer serial = 1;

    @Column(columnDefinition = "JSON")
    private String settings;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = TimezoneConfig.getCurrentVietnamTime();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = TimezoneConfig.getCurrentVietnamTime();
    }
}
