package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilDiagnosticAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EilDiagnosticAnswerRepository extends JpaRepository<EilDiagnosticAnswerEntity, Long> {

    List<EilDiagnosticAnswerEntity> findByDiagnosticAttemptId(Long diagnosticAttemptId);

    List<EilDiagnosticAnswerEntity> findByDiagnosticAttemptIdOrderByQuestionOrderAsc(Long diagnosticAttemptId);

    Optional<EilDiagnosticAnswerEntity> findByDiagnosticAttemptIdAndQuestionId(Long diagnosticAttemptId, Long questionId);

    List<EilDiagnosticAnswerEntity> findByDiagnosticAttemptIdAndSkillId(Long diagnosticAttemptId, Long skillId);

    List<EilDiagnosticAnswerEntity> findByDiagnosticAttemptIdAndIsCorrect(Long diagnosticAttemptId, Boolean isCorrect);

    @Query("SELECT COUNT(a) FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId")
    long countByDiagnosticAttemptId(@Param("attemptId") Long diagnosticAttemptId);

    @Query("SELECT COUNT(a) FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId AND a.isCorrect = true")
    long countCorrectByDiagnosticAttemptId(@Param("attemptId") Long diagnosticAttemptId);

    @Query("SELECT COUNT(a) FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId AND a.skillId = :skillId")
    long countByDiagnosticAttemptIdAndSkillId(@Param("attemptId") Long diagnosticAttemptId, @Param("skillId") Long skillId);

    @Query("SELECT COUNT(a) FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId AND a.skillId = :skillId AND a.isCorrect = true")
    long countCorrectByDiagnosticAttemptIdAndSkillId(@Param("attemptId") Long diagnosticAttemptId, @Param("skillId") Long skillId);

    @Query("SELECT DISTINCT a.skillId FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId")
    List<Long> findDistinctSkillIdsByDiagnosticAttemptId(@Param("attemptId") Long diagnosticAttemptId);

    @Query("SELECT DISTINCT a.questionId FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId")
    List<Long> findAnsweredQuestionIdsByAttemptId(@Param("attemptId") Long diagnosticAttemptId);

    @Query("SELECT SUM(a.timeSpentSeconds) FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId")
    Integer getTotalTimeSpentByAttemptId(@Param("attemptId") Long diagnosticAttemptId);

    @Query("SELECT MAX(a.questionOrder) FROM EilDiagnosticAnswerEntity a WHERE a.diagnosticAttemptId = :attemptId")
    Integer getMaxQuestionOrderByAttemptId(@Param("attemptId") Long diagnosticAttemptId);

    void deleteByDiagnosticAttemptId(Long diagnosticAttemptId);
}
