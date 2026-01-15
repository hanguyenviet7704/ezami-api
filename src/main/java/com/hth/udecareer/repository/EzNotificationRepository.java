package com.hth.udecareer.repository;

import com.hth.udecareer.entities.EzNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EzNotificationRepository extends JpaRepository<EzNotificationEntity, Long> {

    Page<EzNotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<EzNotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    Optional<EzNotificationEntity> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE EzNotificationEntity n SET n.isRead = true WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL)")
    int markAllRead(@Param("userId") Long userId);
}

