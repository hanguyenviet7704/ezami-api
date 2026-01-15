package com.hth.udecareer.eil.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity mapping to WordPress wp_ez_skills_versions table.
 * This table stores version history for skills from ezami-admin-tools plugin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wp_ez_skills_versions")
public class WpEzSkillVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "data", columnDefinition = "JSON", nullable = false)
    private String data;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
