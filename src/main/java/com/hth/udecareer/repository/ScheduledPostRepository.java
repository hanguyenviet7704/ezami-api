package com.hth.udecareer.repository;

import com.hth.udecareer.entities.ScheduledPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledPostRepository extends JpaRepository<ScheduledPostEntity, Long> {

    // Find scheduled posts by user
    Page<ScheduledPostEntity> findByUserIdOrderByScheduledAtAsc(Long userId, Pageable pageable);

    // Find scheduled posts by user and status
    Page<ScheduledPostEntity> findByUserIdAndStatusOrderByScheduledAtAsc(Long userId, String status, Pageable pageable);

    // Find posts ready to be published
    @Query("SELECT s FROM ScheduledPostEntity s WHERE s.status = 'scheduled' " +
            "AND s.scheduledAt <= :now ORDER BY s.scheduledAt ASC")
    List<ScheduledPostEntity> findPostsReadyToPublish(@Param("now") LocalDateTime now);

    // Find upcoming scheduled posts for a user
    @Query("SELECT s FROM ScheduledPostEntity s WHERE s.userId = :userId " +
            "AND s.status = 'scheduled' AND s.scheduledAt > :now " +
            "ORDER BY s.scheduledAt ASC")
    Page<ScheduledPostEntity> findUpcomingPosts(@Param("userId") Long userId,
                                                 @Param("now") LocalDateTime now,
                                                 Pageable pageable);

    // Count scheduled posts by user and status
    long countByUserIdAndStatus(Long userId, String status);

    // Find by user and ID (for ownership check)
    @Query("SELECT s FROM ScheduledPostEntity s WHERE s.id = :id AND s.userId = :userId")
    ScheduledPostEntity findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // Find scheduled posts by space
    Page<ScheduledPostEntity> findBySpaceIdAndStatusOrderByScheduledAtAsc(Long spaceId, String status, Pageable pageable);

    // Count posts ready to publish
    @Query("SELECT COUNT(s) FROM ScheduledPostEntity s WHERE s.status = 'scheduled' AND s.scheduledAt <= :now")
    long countPostsReadyToPublish(@Param("now") LocalDateTime now);
}
