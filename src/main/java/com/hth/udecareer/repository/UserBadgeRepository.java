package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserBadgeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity, Long> {

    // Find all badges for a user
    List<UserBadgeEntity> findByUserIdOrderByEarnedAtDesc(Long userId);

    // Find featured badges for a user
    List<UserBadgeEntity> findByUserIdAndIsFeaturedTrueOrderByEarnedAtDesc(Long userId);

    // Find with pagination
    Page<UserBadgeEntity> findByUserIdOrderByEarnedAtDesc(Long userId, Pageable pageable);

    // Check if user has a specific badge
    Optional<UserBadgeEntity> findByUserIdAndBadgeId(Long userId, Long badgeId);

    // Check if user has badge (exists)
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    // Count badges for a user
    long countByUserId(Long userId);

    // Find users who have a specific badge
    Page<UserBadgeEntity> findByBadgeIdOrderByEarnedAtDesc(Long badgeId, Pageable pageable);

    // Count users who have a specific badge
    long countByBadgeId(Long badgeId);

    // Get badge IDs that user already has
    @Query("SELECT ub.badgeId FROM UserBadgeEntity ub WHERE ub.userId = :userId")
    List<Long> findBadgeIdsByUserId(@Param("userId") Long userId);

    // Get recent badge earners
    @Query("SELECT ub FROM UserBadgeEntity ub ORDER BY ub.earnedAt DESC")
    Page<UserBadgeEntity> findRecentBadgeEarners(Pageable pageable);

    // Count certificate-based badges for a user
    @Query("SELECT COUNT(ub) FROM UserBadgeEntity ub " +
            "JOIN BadgeEntity b ON ub.badgeId = b.id " +
            "WHERE ub.userId = :userId AND b.requirementType IN :reqTypes")
    long countCertificateBadgesByUserId(@Param("userId") Long userId, @Param("reqTypes") List<String> requirementTypes);
}
