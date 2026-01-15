package com.hth.udecareer.repository;

import com.hth.udecareer.entities.FcomUserActivitiesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FcomUserActivityRepository extends JpaRepository<FcomUserActivitiesEntity, Long> {
    boolean existsByUserIdAndActionNameAndRelatedId(Long userId, String actionName, Long relatedId);

    boolean existsByUserIdAndActionNameAndFeedId(Long id, String actionName, Long feedId);

    List<FcomUserActivitiesEntity> findByUserIdAndActionNameAndRelatedId(Long userId, String actionName, Long relatedId);

    List<FcomUserActivitiesEntity> findByUserIdAndActionNameAndFeedId(Long userId, String actionName, Long feedId);

    Page<FcomUserActivitiesEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT a FROM FcomUserActivitiesEntity a WHERE a.userId = :userId AND a.actionName LIKE 'POINT_%' ORDER BY a.createdAt DESC")
    Page<FcomUserActivitiesEntity> findPointHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
}
