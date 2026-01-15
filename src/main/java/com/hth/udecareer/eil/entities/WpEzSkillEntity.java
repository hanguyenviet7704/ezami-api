package com.hth.udecareer.eil.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity mapping to WordPress wp_ez_skills table.
 * This table stores skill taxonomy from ezami-admin-tools plugin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wp_ez_skills")
public class WpEzSkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "certification_id", nullable = false, length = 50)
    private String certificationId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 0;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "status", length = 10)
    @Builder.Default
    private String status = "active";

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public boolean isLeaf() {
        // Will be determined by service layer checking if has children
        return level > 0;
    }
}
