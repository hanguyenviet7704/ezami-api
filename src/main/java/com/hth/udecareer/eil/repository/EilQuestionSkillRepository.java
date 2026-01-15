package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilQuestionSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EilQuestionSkillRepository extends JpaRepository<EilQuestionSkillEntity, Long> {

    List<EilQuestionSkillEntity> findByQuestionId(Long questionId);

    List<EilQuestionSkillEntity> findBySkillId(Long skillId);

    Optional<EilQuestionSkillEntity> findByQuestionIdAndSkillId(Long questionId, Long skillId);

    List<EilQuestionSkillEntity> findByQuestionIdAndIsPrimaryTrue(Long questionId);

    Optional<EilQuestionSkillEntity> findFirstByQuestionIdAndIsPrimaryTrue(Long questionId);

    List<EilQuestionSkillEntity> findBySkillIdAndDifficulty(Long skillId, Integer difficulty);

    @Query("SELECT qs FROM EilQuestionSkillEntity qs WHERE qs.skillId = :skillId AND qs.difficulty BETWEEN :minDiff AND :maxDiff")
    List<EilQuestionSkillEntity> findBySkillIdAndDifficultyBetween(
            @Param("skillId") Long skillId,
            @Param("minDiff") Integer minDifficulty,
            @Param("maxDiff") Integer maxDifficulty);

    @Query("SELECT qs.questionId FROM EilQuestionSkillEntity qs WHERE qs.skillId = :skillId")
    List<Long> findQuestionIdsBySkillId(@Param("skillId") Long skillId);

    @Query("SELECT qs.questionId FROM EilQuestionSkillEntity qs WHERE qs.skillId = :skillId AND qs.difficulty = :difficulty")
    List<Long> findQuestionIdsBySkillIdAndDifficulty(@Param("skillId") Long skillId, @Param("difficulty") Integer difficulty);

    @Query("SELECT DISTINCT qs.questionId FROM EilQuestionSkillEntity qs WHERE qs.skillId IN :skillIds")
    List<Long> findQuestionIdsBySkillIds(@Param("skillIds") List<Long> skillIds);

    @Query("SELECT DISTINCT qs.skillId FROM EilQuestionSkillEntity qs WHERE qs.questionId = :questionId")
    List<Long> findSkillIdsByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(DISTINCT qs.questionId) FROM EilQuestionSkillEntity qs WHERE qs.skillId = :skillId")
    long countQuestionsBySkillId(@Param("skillId") Long skillId);

    @Query("SELECT COUNT(DISTINCT qs.questionId) FROM EilQuestionSkillEntity qs WHERE qs.skillId = :skillId AND qs.difficulty = :difficulty")
    long countQuestionsBySkillIdAndDifficulty(@Param("skillId") Long skillId, @Param("difficulty") Integer difficulty);

    @Query("SELECT qs FROM EilQuestionSkillEntity qs " +
           "JOIN EilSkillEntity s ON qs.skillId = s.id " +
           "WHERE s.category = :category")
    List<EilQuestionSkillEntity> findBySkillCategory(@Param("category") String category);

    @Query("SELECT DISTINCT qs.questionId FROM EilQuestionSkillEntity qs " +
           "JOIN EilSkillEntity s ON qs.skillId = s.id " +
           "WHERE s.category = :category")
    List<Long> findQuestionIdsBySkillCategory(@Param("category") String category);

    void deleteByQuestionId(Long questionId);
}
