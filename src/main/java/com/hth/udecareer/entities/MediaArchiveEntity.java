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

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_media_archive")
public class MediaArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_source", length = 50)
    private String objectSource;

    @Column(name = "media_key", unique = true)
    private String mediaKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "feed_id")
    private Long feedId;

    @Column(name = "sub_object_id")
    private Long subObjectId;

    @Column(name = "media_type", length = 100)
    private String mediaType;

    @Column(name = "driver", length = 50)
    private String driver;

    @Column(name = "media_path")
    private String mediaPath;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Integer isActive = 1;

    @Column(name = "settings", columnDefinition = "JSON")
    private String settings;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

