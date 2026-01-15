package com.hth.udecareer.eil.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wp_ez_certifications")
public class WpEzCertificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "certification_id", unique = true, nullable = false, length = 100)
    private String certificationId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "acronym", length = 20)
    private String acronym;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "vendor", length = 100)
    private String vendor;

    @Column(name = "exam_code", length = 50)
    private String examCode;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Column(name = "passing_score")
    private Double passingScore;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column(name = "question_format", length = 100)
    private String questionFormat;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "validity_years")
    private Integer validityYears;

    @Column(name = "official_url", length = 500)
    private String officialUrl;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
