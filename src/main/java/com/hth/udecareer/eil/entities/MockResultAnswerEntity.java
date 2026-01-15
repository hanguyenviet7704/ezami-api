package com.hth.udecareer.eil.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "eil_mock_test_result_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockResultAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_result_id", nullable = false)
    @ToString.Exclude
    private MockResultEntity mockResult;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "points_earned")
    private Double pointsEarned;

    @Column(name = "max_points")
    private Double maxPoints;
}
