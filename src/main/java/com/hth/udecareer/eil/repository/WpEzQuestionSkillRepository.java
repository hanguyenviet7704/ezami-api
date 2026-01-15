package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzQuestionSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WpEzQuestionSkillRepository extends JpaRepository<WpEzQuestionSkillEntity, Long> {

    // Find by question
    List<WpEzQuestionSkillEntity> findByQuestionId(Long questionId);

    Optional<WpEzQuestionSkillEntity> findByQuestionIdAndSkillId(Long questionId, Long skillId);

    // Find by skill
    List<WpEzQuestionSkillEntity> findBySkillId(Long skillId);

    List<WpEzQuestionSkillEntity> findBySkillIdIn(List<Long> skillIds);

    // Find by confidence
    List<WpEzQuestionSkillEntity> findByConfidence(String confidence);

    List<WpEzQuestionSkillEntity> findBySkillIdAndConfidence(Long skillId, String confidence);

    // Count mappings
    long countBySkillId(Long skillId);

    long countByQuestionId(Long questionId);

    long countByConfidence(String confidence);

    // Get questions for a skill with high/medium confidence
    @Query("SELECT qs.questionId FROM WpEzQuestionSkillEntity qs WHERE qs.skillId = :skillId AND qs.confidence IN ('high', 'medium') ORDER BY qs.weight DESC")
    List<Long> findConfidentQuestionIdsBySkillId(@Param("skillId") Long skillId);

    // Get primary skill for a question (highest weight)
    @Query("SELECT qs FROM WpEzQuestionSkillEntity qs WHERE qs.questionId = :questionId ORDER BY qs.weight DESC")
    List<WpEzQuestionSkillEntity> findByQuestionIdOrderByWeightDesc(@Param("questionId") Long questionId);

    // Get all question IDs mapped to skills in a certification
    @Query("SELECT DISTINCT qs.questionId FROM WpEzQuestionSkillEntity qs " +
           "JOIN WpEzSkillEntity s ON qs.skillId = s.id " +
           "WHERE s.certificationId = :certId AND s.status = 'active'")
    List<Long> findQuestionIdsByCertification(@Param("certId") String certificationId);

    // Count questions per skill for a certification
    @Query("SELECT qs.skillId, COUNT(DISTINCT qs.questionId) FROM WpEzQuestionSkillEntity qs " +
           "JOIN WpEzSkillEntity s ON qs.skillId = s.id " +
           "WHERE s.certificationId = :certId AND s.status = 'active' " +
           "GROUP BY qs.skillId")
    List<Object[]> countQuestionsBySkillForCertification(@Param("certId") String certificationId);

    // Get skill IDs for a question
    @Query("SELECT qs.skillId FROM WpEzQuestionSkillEntity qs WHERE qs.questionId = :questionId")
    List<Long> findSkillIdsByQuestionId(@Param("questionId") Long questionId);

    // Delete mappings by question
    void deleteByQuestionId(Long questionId);

    // Delete mappings by skill
    void deleteBySkillId(Long skillId);
}
