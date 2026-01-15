package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzCertificationCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WpEzCertificationCategoryRepository extends JpaRepository<WpEzCertificationCategoryEntity, Integer> {

    Optional<WpEzCertificationCategoryEntity> findByCategoryCode(String categoryCode);

    List<WpEzCertificationCategoryEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<WpEzCertificationCategoryEntity> findByParentCategoryCodeAndIsActiveTrue(String parentCategoryCode);

    @Query("SELECT c FROM WpEzCertificationCategoryEntity c WHERE c.parentCategoryCode IS NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<WpEzCertificationCategoryEntity> findRootCategories();

    @Query("SELECT c FROM WpEzCertificationCategoryEntity c WHERE c.parentCategoryCode IS NOT NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<WpEzCertificationCategoryEntity> findSubCategories();
}
