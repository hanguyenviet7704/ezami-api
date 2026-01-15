package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilPatternAnalysisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EilPatternAnalysisRepository extends JpaRepository<EilPatternAnalysisEntity, Long> {

    Optional<EilPatternAnalysisEntity> findBySessionId(String sessionId);

    List<EilPatternAnalysisEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<EilPatternAnalysisEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<EilPatternAnalysisEntity> findByUserIdAndCertificationCodeOrderByCreatedAtDesc(
            Long userId, String certificationCode, Pageable pageable);

    List<EilPatternAnalysisEntity> findByUserIdAndCertificationCodeOrderByCreatedAtDesc(
            Long userId, String certificationCode);

    @Query("SELECT p FROM EilPatternAnalysisEntity p WHERE p.userId = :userId " +
           "AND p.createdAt >= :startDate ORDER BY p.createdAt DESC")
    List<EilPatternAnalysisEntity> findRecentByUserId(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(p.accuracy) FROM EilPatternAnalysisEntity p " +
           "WHERE p.userId = :userId AND p.sessionHour = :hour")
    Double getAverageAccuracyByHour(@Param("userId") Long userId, @Param("hour") Integer hour);

    @Query("SELECT p.timeOfDay, AVG(p.accuracy), COUNT(p) FROM EilPatternAnalysisEntity p " +
           "WHERE p.userId = :userId GROUP BY p.timeOfDay")
    List<Object[]> getAccuracyByTimeOfDay(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM EilPatternAnalysisEntity p " +
           "WHERE p.userId = :userId AND p.fatigueDetected = true")
    Long countFatigueSessionsByUserId(@Param("userId") Long userId);

    void deleteBySessionId(String sessionId);

    void deleteByUserId(Long userId);
}
