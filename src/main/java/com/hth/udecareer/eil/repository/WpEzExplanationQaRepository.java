package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzExplanationQaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WpEzExplanationQaRepository extends JpaRepository<WpEzExplanationQaEntity, Long> {

    // Find by question
    Optional<WpEzExplanationQaEntity> findByQuestionId(Long questionId);

    List<WpEzExplanationQaEntity> findByQuestionIdIn(List<Long> questionIds);

    // Find by rating
    Page<WpEzExplanationQaEntity> findByRatingOrderByCreatedAtDesc(String rating, Pageable pageable);

    // Find unreviewed
    @Query("SELECT e FROM WpEzExplanationQaEntity e WHERE e.reviewedAt IS NULL ORDER BY e.createdAt DESC")
    Page<WpEzExplanationQaEntity> findUnreviewed(Pageable pageable);

    // Find reviewed by reviewer
    Page<WpEzExplanationQaEntity> findByReviewerIdOrderByReviewedAtDesc(Long reviewerId, Pageable pageable);

    // Find by prompt version
    Page<WpEzExplanationQaEntity> findByPromptVersionOrderByCreatedAtDesc(String promptVersion, Pageable pageable);

    // Find by model
    Page<WpEzExplanationQaEntity> findByModelOrderByCreatedAtDesc(String model, Pageable pageable);

    // Count by rating
    long countByRating(String rating);

    // Count unreviewed
    @Query("SELECT COUNT(e) FROM WpEzExplanationQaEntity e WHERE e.reviewedAt IS NULL")
    long countUnreviewed();

    // Stats by prompt version
    @Query("SELECT e.promptVersion, e.rating, COUNT(e) FROM WpEzExplanationQaEntity e " +
           "WHERE e.rating IS NOT NULL GROUP BY e.promptVersion, e.rating ORDER BY e.promptVersion")
    List<Object[]> getStatsByPromptVersion();

    // Average latency by model
    @Query("SELECT e.model, AVG(e.latencyMs), AVG(e.tokensUsed) FROM WpEzExplanationQaEntity e " +
           "WHERE e.model IS NOT NULL GROUP BY e.model ORDER BY e.model")
    List<Object[]> getPerformanceStatsByModel();

    // Search by question text
    @Query("SELECT e FROM WpEzExplanationQaEntity e WHERE LOWER(e.questionText) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY e.createdAt DESC")
    Page<WpEzExplanationQaEntity> searchByQuestionText(@Param("keyword") String keyword, Pageable pageable);
}
