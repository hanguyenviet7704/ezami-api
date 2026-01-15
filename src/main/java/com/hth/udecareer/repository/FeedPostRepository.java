package com.hth.udecareer.repository;

import com.hth.udecareer.entities.FeedPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FeedPostRepository extends JpaRepository<FeedPostEntity, Long> {

    Optional<FeedPostEntity> findBySlug(String slug);

    Page<FeedPostEntity> findBySpaceIdAndStatus(Long spaceId, String status, Pageable pageable);

    Page<FeedPostEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @Query("SELECT DISTINCT f FROM FeedPostEntity f " +
           "LEFT JOIN TermFeedEntity tf ON tf.postId = f.id " +
           "WHERE f.status = 'published' " +
           "AND (:spaceId IS NULL OR f.spaceId = :spaceId) " +
           "AND (:userId IS NULL OR f.userId = :userId) " +
           "AND (:search IS NULL OR f.title LIKE CONCAT('%', :search, '%') OR f.message LIKE CONCAT('%', :search, '%')) " +
           "AND (:termId IS NULL OR tf.termId = :termId)")
    Page<FeedPostEntity> findFeedsWithFilters(
            @Param("spaceId") Long spaceId,
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("termId") Long termId,
            Pageable pageable);

    @Query("SELECT COUNT(f) FROM FeedPostEntity f WHERE f.status = 'published'")
    long countPublishedFeeds();

    @Query("SELECT f FROM FeedPostEntity f WHERE f.status = 'published' " +
           "AND f.isSticky = 1 " +
           "ORDER BY f.priority DESC, f.createdAt DESC")
    Optional<FeedPostEntity> findStickyFeed();

    @Query("SELECT COUNT(f) FROM FeedPostEntity f WHERE f.userId = :userId " +
           "AND f.status = 'published' " +
           "AND f.createdAt >= :startOfDay")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(f) FROM FeedPostEntity f WHERE f.userId = :userId " +
           "AND f.status = 'published'")
    long countByUserIdAndStatusPublished(@Param("userId") Long userId);
}

