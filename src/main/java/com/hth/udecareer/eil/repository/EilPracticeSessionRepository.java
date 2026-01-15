package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilPracticeSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EilPracticeSessionRepository extends JpaRepository<EilPracticeSessionEntity, Long> {

    Optional<EilPracticeSessionEntity> findBySessionId(String sessionId);

    List<EilPracticeSessionEntity> findByUserId(Long userId);

    Page<EilPracticeSessionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<EilPracticeSessionEntity> findByUserIdAndStatus(Long userId, String status);

    Optional<EilPracticeSessionEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    @Query("SELECT p FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<EilPracticeSessionEntity> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'COMPLETED' ORDER BY p.endTime DESC")
    List<EilPracticeSessionEntity> findCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.sessionType = :type ORDER BY p.createdAt DESC")
    List<EilPracticeSessionEntity> findByUserIdAndSessionType(@Param("userId") Long userId, @Param("type") String sessionType);

    @Query("SELECT p FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.targetSkillId = :skillId ORDER BY p.createdAt DESC")
    List<EilPracticeSessionEntity> findByUserIdAndTargetSkillId(@Param("userId") Long userId, @Param("skillId") Long skillId);

    @Query("SELECT COUNT(p) FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.totalQuestions) FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Long getTotalQuestionsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.correctCount) FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Long getTotalCorrectByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.totalTimeSeconds) FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Long getTotalTimeByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM EilPracticeSessionEntity p WHERE p.userId = :userId AND p.status = 'COMPLETED' AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<EilPracticeSessionEntity> findCompletedByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE EilPracticeSessionEntity p SET p.status = :status WHERE p.sessionId = :sessionId")
    int updateStatus(@Param("sessionId") String sessionId, @Param("status") String status);

    @Modifying
    @Query("UPDATE EilPracticeSessionEntity p SET p.totalQuestions = p.totalQuestions + 1 WHERE p.sessionId = :sessionId")
    int incrementTotalQuestions(@Param("sessionId") String sessionId);

    @Modifying
    @Query("UPDATE EilPracticeSessionEntity p SET p.correctCount = p.correctCount + 1 WHERE p.sessionId = :sessionId")
    int incrementCorrectCount(@Param("sessionId") String sessionId);

    boolean existsBySessionId(String sessionId);
}
