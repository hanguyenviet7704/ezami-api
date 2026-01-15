package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilDiagnosticAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EilDiagnosticAttemptRepository extends JpaRepository<EilDiagnosticAttemptEntity, Long> {

    Optional<EilDiagnosticAttemptEntity> findBySessionId(String sessionId);

    List<EilDiagnosticAttemptEntity> findByUserId(Long userId);

    List<EilDiagnosticAttemptEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<EilDiagnosticAttemptEntity> findByUserIdAndStatus(Long userId, String status);

    Optional<EilDiagnosticAttemptEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    @Query("SELECT d FROM EilDiagnosticAttemptEntity d WHERE d.userId = :userId AND d.status = 'IN_PROGRESS' ORDER BY d.createdAt DESC")
    List<EilDiagnosticAttemptEntity> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM EilDiagnosticAttemptEntity d WHERE d.userId = :userId AND d.status = 'COMPLETED' ORDER BY d.endTime DESC")
    List<EilDiagnosticAttemptEntity> findCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM EilDiagnosticAttemptEntity d WHERE d.userId = :userId AND d.testType = :testType AND d.status = 'COMPLETED' ORDER BY d.endTime DESC")
    List<EilDiagnosticAttemptEntity> findCompletedByUserIdAndTestType(@Param("userId") Long userId, @Param("testType") String testType);

    Optional<EilDiagnosticAttemptEntity> findFirstByUserIdAndTestTypeAndStatusOrderByCreatedAtDesc(
            Long userId, String testType, String status);

    @Query("SELECT COUNT(d) FROM EilDiagnosticAttemptEntity d WHERE d.userId = :userId AND d.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE EilDiagnosticAttemptEntity d SET d.status = :status WHERE d.sessionId = :sessionId")
    int updateStatus(@Param("sessionId") String sessionId, @Param("status") String status);

    @Modifying
    @Query("UPDATE EilDiagnosticAttemptEntity d SET d.answeredQuestions = d.answeredQuestions + 1 WHERE d.sessionId = :sessionId")
    int incrementAnsweredQuestions(@Param("sessionId") String sessionId);

    boolean existsBySessionId(String sessionId);
}
