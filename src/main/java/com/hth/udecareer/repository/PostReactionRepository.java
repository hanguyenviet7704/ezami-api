package com.hth.udecareer.repository;

import com.hth.udecareer.entities.PostReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReactionEntity, Long> {

    Optional<PostReactionEntity> findByUserIdAndObjectIdAndObjectType(Long userId, Long objectId, String objectType);

    Optional<PostReactionEntity> findByUserIdAndObjectIdAndObjectTypeAndType(Long userId, Long objectId, String objectType, String type);

    List<PostReactionEntity> findByObjectIdAndObjectTypeAndType(Long objectId, String objectType, String type);

    @Query("SELECT COUNT(r) FROM PostReactionEntity r WHERE r.objectId = :objectId AND r.objectType = :objectType")
    long countByObjectIdAndObjectType(@Param("objectId") Long objectId, @Param("objectType") String objectType);

    @Query("SELECT COUNT(r) FROM PostReactionEntity r WHERE r.objectId = :objectId AND r.objectType = :objectType AND r.type = :type")
    long countByObjectIdAndObjectTypeAndType(@Param("objectId") Long objectId, @Param("objectType") String objectType, @Param("type") String type);

    @Query("SELECT COUNT(r) FROM PostReactionEntity r WHERE r.userId = :userId " +
           "AND r.createdAt >= :startOfDay")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
}

