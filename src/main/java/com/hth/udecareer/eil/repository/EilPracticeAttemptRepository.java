package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilPracticeAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EilPracticeAttemptRepository extends JpaRepository<EilPracticeAttemptEntity, Long> {

    List<EilPracticeAttemptEntity> findBySessionId(Long sessionId);

    List<EilPracticeAttemptEntity> findBySessionIdOrderByQuestionOrderAsc(Long sessionId);

    Optional<EilPracticeAttemptEntity> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    List<EilPracticeAttemptEntity> findBySessionIdAndSkillId(Long sessionId, Long skillId);

    List<EilPracticeAttemptEntity> findBySessionIdAndIsCorrect(Long sessionId, Boolean isCorrect);

    @Query("SELECT COUNT(a) FROM EilPracticeAttemptEntity a WHERE a.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(a) FROM EilPracticeAttemptEntity a WHERE a.sessionId = :sessionId AND a.isCorrect = true")
    long countCorrectBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT DISTINCT a.questionId FROM EilPracticeAttemptEntity a WHERE a.sessionId = :sessionId")
    List<Long> findAnsweredQuestionIdsBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT MAX(a.questionOrder) FROM EilPracticeAttemptEntity a WHERE a.sessionId = :sessionId")
    Integer getMaxQuestionOrderBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT a FROM EilPracticeAttemptEntity a " +
           "JOIN EilPracticeSessionEntity s ON a.sessionId = s.id " +
           "WHERE s.userId = :userId ORDER BY a.answeredAt DESC")
    List<EilPracticeAttemptEntity> findRecentByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT a.questionId FROM EilPracticeAttemptEntity a " +
           "JOIN EilPracticeSessionEntity s ON a.sessionId = s.id " +
           "WHERE s.userId = :userId AND a.answeredAt >= :since")
    List<Long> findRecentlyAnsweredQuestionIdsByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT a.questionId FROM EilPracticeAttemptEntity a " +
           "JOIN EilPracticeSessionEntity s ON a.sessionId = s.id " +
           "WHERE s.userId = :userId ORDER BY a.answeredAt DESC")
    List<Long> findQuestionIdsOrderedByRecency(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM EilPracticeAttemptEntity a " +
           "JOIN EilPracticeSessionEntity s ON a.sessionId = s.id " +
           "WHERE s.userId = :userId AND a.skillId = :skillId")
    long countByUserIdAndSkillId(@Param("userId") Long userId, @Param("skillId") Long skillId);

    @Query("SELECT COUNT(a) FROM EilPracticeAttemptEntity a " +
           "JOIN EilPracticeSessionEntity s ON a.sessionId = s.id " +
           "WHERE s.userId = :userId AND a.skillId = :skillId AND a.isCorrect = true")
    long countCorrectByUserIdAndSkillId(@Param("userId") Long userId, @Param("skillId") Long skillId);

    @Query("SELECT SUM(a.timeSpentSeconds) FROM EilPracticeAttemptEntity a WHERE a.sessionId = :sessionId")
    Integer getTotalTimeSpentBySessionId(@Param("sessionId") Long sessionId);

    void deleteBySessionId(Long sessionId);
}
