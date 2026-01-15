package com.hth.udecareer.repository;

import com.hth.udecareer.entities.NotificationUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUserEntity, Long> {

    List<NotificationUserEntity> findByUserId(Long userId);

    List<NotificationUserEntity> findByUserIdAndIsRead(Long userId, Integer isRead);

    Optional<NotificationUserEntity> findByUserIdAndObjectId(Long userId, Long objectId);

    @Query("SELECT COUNT(nu) FROM NotificationUserEntity nu WHERE nu.userId = :userId AND nu.isRead = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE NotificationUserEntity nu SET nu.isRead = 1, nu.updatedAt = CURRENT_TIMESTAMP WHERE nu.userId = :userId AND nu.isRead = 0")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);

    List<NotificationUserEntity> findByObjectId(Long objectId);
}
