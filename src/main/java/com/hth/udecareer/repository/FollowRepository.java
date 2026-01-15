package com.hth.udecareer.repository;

import com.hth.udecareer.entities.FollowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    /**
     * Find a follow/block relationship between two users
     */
    Optional<FollowEntity> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Get all users that a specific user is following (level >= 1)
     */
    @Query("SELECT f FROM FollowEntity f WHERE f.followerId = :userId AND f.level >= 1")
    Page<FollowEntity> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get all followers of a specific user (level >= 1)
     */
    @Query("SELECT f FROM FollowEntity f WHERE f.followedId = :userId AND f.level >= 1")
    Page<FollowEntity> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get all users blocked by a specific user (level = 0)
     */
    @Query("SELECT f FROM FollowEntity f WHERE f.followerId = :userId AND f.level = 0")
    Page<FollowEntity> findBlockedByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count followers of a user (level >= 1)
     */
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.followedId = :userId AND f.level >= 1")
    long countFollowersByUserId(@Param("userId") Long userId);

    /**
     * Count followings of a user (level >= 1)
     */
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.followerId = :userId AND f.level >= 1")
    long countFollowingsByUserId(@Param("userId") Long userId);

    /**
     * Check if user A is following user B
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FollowEntity f " +
           "WHERE f.followerId = :followerId AND f.followedId = :followedId AND f.level >= 1")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    /**
     * Check if user A has blocked user B
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FollowEntity f " +
           "WHERE f.followerId = :blockerId AND f.followedId = :blockedId AND f.level = 0")
    boolean isBlocked(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    /**
     * Get follow status between two users (for displaying mutual follow status)
     */
    @Query("SELECT f FROM FollowEntity f WHERE " +
           "(f.followerId = :userA AND f.followedId = :userB) OR " +
           "(f.followerId = :userB AND f.followedId = :userA)")
    List<FollowEntity> findMutualFollowStatus(@Param("userA") Long userA, @Param("userB") Long userB);

    /**
     * Delete follow relationship
     */
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
}
