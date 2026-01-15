package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EilSkillRepository extends JpaRepository<EilSkillEntity, Long> {

    Optional<EilSkillEntity> findByCode(String code);

    List<EilSkillEntity> findByCategory(String category);

    List<EilSkillEntity> findByCategoryAndIsActiveTrue(String category);

    List<EilSkillEntity> findByLevel(Integer level);

    List<EilSkillEntity> findByLevelAndIsActiveTrue(Integer level);

    List<EilSkillEntity> findByParentId(Long parentId);

    List<EilSkillEntity> findByParentIdAndIsActiveTrue(Long parentId);

    List<EilSkillEntity> findByCategoryAndLevel(String category, Integer level);

    List<EilSkillEntity> findByIsActiveTrueOrderByPriorityAsc();

    @Query("SELECT s FROM EilSkillEntity s WHERE s.level = 3 AND s.isActive = true ORDER BY s.category, s.priority")
    List<EilSkillEntity> findAllLeafSkills();

    @Query("SELECT s FROM EilSkillEntity s WHERE s.category = :category AND s.level = 3 AND s.isActive = true ORDER BY s.priority")
    List<EilSkillEntity> findLeafSkillsByCategory(@Param("category") String category);

    @Query("SELECT s FROM EilSkillEntity s WHERE s.subcategory = :subcategory AND s.level = 3 AND s.isActive = true ORDER BY s.priority")
    List<EilSkillEntity> findLeafSkillsBySubcategory(@Param("subcategory") String subcategory);

    @Query("SELECT DISTINCT s.category FROM EilSkillEntity s WHERE s.isActive = true")
    List<String> findAllActiveCategories();

    @Query("SELECT DISTINCT s.subcategory FROM EilSkillEntity s WHERE s.category = :category AND s.subcategory IS NOT NULL AND s.isActive = true")
    List<String> findSubcategoriesByCategory(@Param("category") String category);

    @Query("SELECT s FROM EilSkillEntity s WHERE s.id IN :ids AND s.isActive = true")
    List<EilSkillEntity> findByIdInAndIsActiveTrue(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(s) FROM EilSkillEntity s WHERE s.level = 3 AND s.isActive = true")
    long countLeafSkills();

    @Query("SELECT COUNT(s) FROM EilSkillEntity s WHERE s.category = :category AND s.level = 3 AND s.isActive = true")
    long countLeafSkillsByCategory(@Param("category") String category);
}
