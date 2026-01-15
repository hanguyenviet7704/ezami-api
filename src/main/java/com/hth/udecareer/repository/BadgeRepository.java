package com.hth.udecareer.repository;

import com.hth.udecareer.entities.BadgeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<BadgeEntity, Long> {

    // Find by slug
    Optional<BadgeEntity> findBySlug(String slug);

    // Find all active badges
    List<BadgeEntity> findByIsActiveTrueOrderByPriorityAsc();

    // Find badges by type
    List<BadgeEntity> findByTypeAndIsActiveTrueOrderByPriorityAsc(String type);

    // Find badges by requirement type
    List<BadgeEntity> findByRequirementTypeAndIsActiveTrueOrderByRequirementValueAsc(String requirementType);

    // Find badges with pagination
    Page<BadgeEntity> findByIsActiveTrueOrderByPriorityAsc(Pageable pageable);

    // Find all badges (including inactive) for admin
    Page<BadgeEntity> findAllByOrderByPriorityAsc(Pageable pageable);

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Find earnable badges for a requirement type and value
    @Query("SELECT b FROM BadgeEntity b WHERE b.requirementType = :reqType " +
            "AND b.requirementValue <= :value AND b.isActive = true ORDER BY b.requirementValue DESC")
    List<BadgeEntity> findEarnableBadges(@Param("reqType") String requirementType, @Param("value") int value);

    // Find badges by multiple requirement types (for certificate-based badges)
    @Query("SELECT b FROM BadgeEntity b WHERE b.requirementType IN :reqTypes AND b.isActive = true ORDER BY b.priority ASC")
    List<BadgeEntity> findByRequirementTypeInAndIsActiveTrue(@Param("reqTypes") List<String> requirementTypes);
}
