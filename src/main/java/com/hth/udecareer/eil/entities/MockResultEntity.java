package com.hth.udecareer.eil.entities;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eil_mock_test_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "certificate_code", nullable = false, length = 50)
    private String certificateCode;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "time_spent_seconds", nullable = false)
    private Integer timeSpentSeconds;

    @Column(name = "percentage_score")
    private Double percentageScore;

    @Column(name = "passed")
    private Boolean passed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "mockResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MockResultAnswerEntity> answers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (percentageScore == null && totalQuestions > 0) {
            percentageScore = (correctCount * 100.0) / totalQuestions;
        }
        if (passed == null && percentageScore != null) {
            // Default passing score is 80%
            passed = percentageScore >= 80.0;
        }
    }
}
