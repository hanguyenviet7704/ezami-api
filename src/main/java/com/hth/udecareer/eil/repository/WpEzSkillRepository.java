package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WpEzSkillRepository extends JpaRepository<WpEzSkillEntity, Long> {

    // Find by certification
    List<WpEzSkillEntity> findByCertificationIdAndStatusOrderBySortOrderAsc(String certificationId, String status);

    List<WpEzSkillEntity> findByCertificationIdOrderBySortOrderAsc(String certificationId);

    // Find root skills (no parent)
    @Query("SELECT s FROM WpEzSkillEntity s WHERE s.parentId IS NULL AND s.status = 'active' ORDER BY s.sortOrder")
    List<WpEzSkillEntity> findAllRootSkills();

    @Query("SELECT s FROM WpEzSkillEntity s WHERE s.certificationId = :certId AND s.parentId IS NULL AND s.status = 'active' ORDER BY s.sortOrder")
    List<WpEzSkillEntity> findRootSkillsByCertification(@Param("certId") String certificationId);

    // Find children of a skill
    List<WpEzSkillEntity> findByParentIdAndStatusOrderBySortOrderAsc(Long parentId, String status);

    List<WpEzSkillEntity> findByParentIdOrderBySortOrderAsc(Long parentId);

    // Find by code
    Optional<WpEzSkillEntity> findByCodeAndCertificationId(String code, String certificationId);

    Optional<WpEzSkillEntity> findByCode(String code);

    // Count skills
    long countByCertificationIdAndStatus(String certificationId, String status);

    long countByParentId(Long parentId);

    // Find leaf skills (skills with no children)
    @Query("SELECT s FROM WpEzSkillEntity s WHERE s.certificationId = :certId AND s.status = 'active' " +
           "AND NOT EXISTS (SELECT c FROM WpEzSkillEntity c WHERE c.parentId = s.id) " +
           "ORDER BY s.sortOrder")
    List<WpEzSkillEntity> findLeafSkillsByCertification(@Param("certId") String certificationId);

    // Get distinct certifications
    @Query("SELECT DISTINCT s.certificationId FROM WpEzSkillEntity s WHERE s.status = 'active' ORDER BY s.certificationId")
    List<String> findDistinctCertificationIds();

    // Count skills by certification
    @Query("SELECT s.certificationId, COUNT(s) FROM WpEzSkillEntity s WHERE s.status = 'active' GROUP BY s.certificationId ORDER BY COUNT(s) DESC")
    List<Object[]> countSkillsByCertification();

    // Find skills by level
    List<WpEzSkillEntity> findByCertificationIdAndLevelAndStatusOrderBySortOrderAsc(
            String certificationId, Integer level, String status);

    // Search skills by name
    @Query("SELECT s FROM WpEzSkillEntity s WHERE s.certificationId = :certId AND s.status = 'active' " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.sortOrder")
    List<WpEzSkillEntity> searchSkillsByCertification(@Param("certId") String certificationId, @Param("keyword") String keyword);
}
