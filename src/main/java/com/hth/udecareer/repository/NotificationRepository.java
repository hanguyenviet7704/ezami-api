package com.hth.udecareer.repository;

import com.hth.udecareer.entities.NotificationEntity;
import com.hth.udecareer.model.dto.NotificationWithStatusDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // NEW METHODS that return DTO with isRead status
    @Query("SELECT new com.hth.udecareer.model.dto.NotificationWithStatusDTO(" +
           "n.id, n.title, n.content, n.action, n.route, n.objectId, n.srcUserId, n.srcObjectType, n.feedId, " +
           "n.createdAt, n.updatedAt, " +
           "CASE WHEN nu.isRead = 1 THEN true ELSE false END, " +
           "nu.updatedAt) " +
           "FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId " +
           "ORDER BY nu.createdAt DESC")
    Page<NotificationWithStatusDTO> findNotificationDTOsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.hth.udecareer.model.dto.NotificationWithStatusDTO(" +
           "n.id, n.title, n.content, n.action, n.route, n.objectId, n.srcUserId, n.srcObjectType, n.feedId, " +
           "n.createdAt, n.updatedAt, " +
           "CASE WHEN nu.isRead = 1 THEN true ELSE false END, " +
           "nu.updatedAt) " +
           "FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId AND nu.isRead = 0 " +
           "ORDER BY nu.createdAt DESC")
    Page<NotificationWithStatusDTO> findUnreadNotificationDTOsForUser(@Param("userId") Long userId, Pageable pageable);

    // ORIGINAL METHODS (for backward compatibility)
    @Query("SELECT n FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId " +
           "ORDER BY nu.createdAt DESC")
    Page<NotificationEntity> findNotificationsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId AND nu.isRead = 0 " +
           "ORDER BY nu.createdAt DESC")
    Page<NotificationEntity> findUnreadNotificationsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId " +
           "ORDER BY nu.createdAt DESC")
    List<NotificationEntity> findNotificationsForUserList(@Param("userId") Long userId);

    @Query("SELECT n FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId AND nu.isRead = 0 " +
           "ORDER BY nu.createdAt DESC")
    List<NotificationEntity> findUnreadNotificationsForUserList(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM NotificationEntity n " +
           "JOIN NotificationUserEntity nu ON n.id = nu.objectId " +
           "WHERE nu.userId = :userId AND nu.isRead = 0")
    long countUnreadNotificationsForUser(@Param("userId") Long userId);

    List<NotificationEntity> findByAction(String action);

    List<NotificationEntity> findByContentContainingIgnoreCase(String content);

    @Deprecated
    default Page<NotificationEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        return findNotificationsForUser(userId, pageable);
    }

    @Deprecated
    default Page<NotificationEntity> findByUserId(Long userId, Pageable pageable) {
        return findNotificationsForUser(userId, pageable);
    }

    @Deprecated
    default List<NotificationEntity> findByUserId(Long userId) {
        return findNotificationsForUserList(userId);
    }

    @Deprecated
    default Page<NotificationEntity> findByUserIdAndIsReadFalse(Long userId, Pageable pageable) {
        return findUnreadNotificationsForUser(userId, pageable);
    }

    @Deprecated
    default List<NotificationEntity> findByUserIdAndIsReadFalse(Long userId) {
        return findUnreadNotificationsForUserList(userId);
    }

    @Deprecated
    default long countByUserIdAndIsReadFalse(Long userId) {
        return countUnreadNotificationsForUser(userId);
    }
}