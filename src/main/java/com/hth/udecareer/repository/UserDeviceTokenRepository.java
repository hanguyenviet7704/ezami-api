package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserDeviceToken;
import com.hth.udecareer.enums.DevicePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho UserDeviceToken
 * Quản lý FCM device tokens của users
 *
 * @author Ezami Team
 * @since 2025-11-27
 */
@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    /**
     * Tìm tất cả active tokens của một user
     */
    List<UserDeviceToken> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Tìm token cụ thể của user
     */
    Optional<UserDeviceToken> findByUserIdAndDeviceToken(Long userId, String deviceToken);

    /**
     * Tìm tất cả tokens của user theo platform
     */
    List<UserDeviceToken> findByUserIdAndPlatformAndIsActiveTrue(Long userId, DevicePlatform platform);

    /**
     * Deactivate tất cả tokens của user (khi logout khỏi tất cả devices)
     */
    @Modifying
    @Query("UPDATE UserDeviceToken t SET t.isActive = false WHERE t.userId = :userId")
    void deactivateAllUserTokens(@Param("userId") Long userId);

    /**
     * Deactivate một token cụ thể
     */
    @Modifying
    @Query("UPDATE UserDeviceToken t SET t.isActive = false WHERE t.userId = :userId AND t.deviceToken = :token")
    void deactivateToken(@Param("userId") Long userId, @Param("token") String token);

    /**
     * Xóa các tokens cũ không active (cleanup task)
     */
    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.isActive = false AND t.updatedAt < :cutoffDate")
    void deleteInactiveTokensOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Đếm số device của user
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Check xem token có tồn tại và active không
     */
    boolean existsByUserIdAndDeviceTokenAndIsActiveTrue(Long userId, String deviceToken);
}

