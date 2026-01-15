package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilExplanationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EilExplanationRepository extends JpaRepository<EilExplanationEntity, Long> {

    Optional<EilExplanationEntity> findByCacheKey(String cacheKey);

    List<EilExplanationEntity> findByQuestionId(Long questionId);

    List<EilExplanationEntity> findByQuestionIdAndLanguage(Long questionId, String language);

    Optional<EilExplanationEntity> findByQuestionIdAndUserAnswerAndLanguageAndVersion(
            Long questionId, String userAnswer, String language, Integer version);

    @Query("SELECT e FROM EilExplanationEntity e WHERE e.cacheKey = :cacheKey AND (e.expiresAt IS NULL OR e.expiresAt > CURRENT_TIMESTAMP)")
    Optional<EilExplanationEntity> findValidByCacheKey(@Param("cacheKey") String cacheKey);

    @Query("SELECT e FROM EilExplanationEntity e WHERE e.expiresAt IS NOT NULL AND e.expiresAt < CURRENT_TIMESTAMP")
    List<EilExplanationEntity> findExpired();

    @Query("SELECT e FROM EilExplanationEntity e ORDER BY e.hitCount DESC")
    List<EilExplanationEntity> findMostAccessed();

    @Modifying
    @Query("UPDATE EilExplanationEntity e SET e.hitCount = e.hitCount + 1, e.lastAccessedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    int incrementHitCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE EilExplanationEntity e SET e.hitCount = e.hitCount + 1, e.lastAccessedAt = CURRENT_TIMESTAMP WHERE e.cacheKey = :cacheKey")
    int incrementHitCountByCacheKey(@Param("cacheKey") String cacheKey);

    @Query("SELECT COUNT(e) FROM EilExplanationEntity e WHERE e.questionId = :questionId")
    long countByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT SUM(e.hitCount) FROM EilExplanationEntity e")
    Long getTotalHitCount();

    @Query("SELECT COUNT(e) FROM EilExplanationEntity e WHERE e.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    void deleteByQuestionId(Long questionId);

    @Modifying
    @Query("DELETE FROM EilExplanationEntity e WHERE e.expiresAt IS NOT NULL AND e.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpired();

    boolean existsByCacheKey(String cacheKey);
}
