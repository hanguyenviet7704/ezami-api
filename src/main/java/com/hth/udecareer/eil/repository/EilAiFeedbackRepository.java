package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilAiFeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EilAiFeedbackRepository extends JpaRepository<EilAiFeedbackEntity, Long> {

    List<EilAiFeedbackEntity> findByUserId(Long userId);

    Page<EilAiFeedbackEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<EilAiFeedbackEntity> findByUserIdAndFeedbackType(Long userId, String feedbackType);

    List<EilAiFeedbackEntity> findByUserIdAndContextTypeAndContextId(Long userId, String contextType, Long contextId);

    Optional<EilAiFeedbackEntity> findFirstByUserIdAndFeedbackTypeOrderByCreatedAtDesc(Long userId, String feedbackType);

    @Query("SELECT f FROM EilAiFeedbackEntity f WHERE f.userId = :userId AND f.feedbackType = :type ORDER BY f.createdAt DESC")
    List<EilAiFeedbackEntity> findRecentByUserIdAndType(@Param("userId") Long userId, @Param("type") String feedbackType);

    @Query("SELECT f FROM EilAiFeedbackEntity f WHERE f.contextType = :contextType AND f.contextId = :contextId")
    List<EilAiFeedbackEntity> findByContext(@Param("contextType") String contextType, @Param("contextId") Long contextId);

    @Query("SELECT AVG(f.userRating) FROM EilAiFeedbackEntity f WHERE f.userId = :userId AND f.userRating IS NOT NULL")
    Double getAverageRatingByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM EilAiFeedbackEntity f WHERE f.userId = :userId AND f.isHelpful = true")
    long countHelpfulByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM EilAiFeedbackEntity f WHERE f.feedbackType = :type")
    long countByFeedbackType(@Param("type") String feedbackType);

    @Query("SELECT SUM(f.tokensUsed) FROM EilAiFeedbackEntity f WHERE f.userId = :userId")
    Long getTotalTokensUsedByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
