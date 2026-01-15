package com.hth.udecareer.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wp_fcom_user_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcomUserActivitiesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "feed_id")
    private Long feedId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "action_name", length = 100)
    private String actionName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
