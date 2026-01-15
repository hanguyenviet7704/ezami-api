package com.hth.udecareer.eil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.eil.entities.WpEzCertificationEntity;

@Repository
public interface WpEzCertificationRepository extends JpaRepository<WpEzCertificationEntity, Integer> {

    Optional<WpEzCertificationEntity> findByCertificationId(String certificationId);

    List<WpEzCertificationEntity> findByIsActiveTrueOrderBySortOrderAsc();

    List<WpEzCertificationEntity> findByCategoryAndIsActiveTrue(String category);

    List<WpEzCertificationEntity> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<WpEzCertificationEntity> findByVendorAndIsActiveTrue(String vendor);

    @Query("SELECT DISTINCT c.category FROM WpEzCertificationEntity c WHERE c.isActive = true ORDER BY c.category")
    List<String> findAllActiveCategories();

    @Query("SELECT DISTINCT c.vendor FROM WpEzCertificationEntity c WHERE c.isActive = true ORDER BY c.vendor")
    List<String> findAllActiveVendors();
}
