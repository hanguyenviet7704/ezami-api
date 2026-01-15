package com.hth.udecareer.service;

import com.hth.udecareer.entities.UserDeviceToken;
import com.hth.udecareer.enums.DevicePlatform;
import com.hth.udecareer.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý device tokens cho push notifications
 *
 * @author Ezami Team
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final UserDeviceTokenRepository deviceTokenRepository;

    @Transactional
    public UserDeviceToken registerDeviceToken(Long userId, String deviceToken,
                                              DevicePlatform platform, String deviceInfo) {

        Optional<UserDeviceToken> existingToken =
                deviceTokenRepository.findByUserIdAndDeviceToken(userId, deviceToken);

        if (existingToken.isPresent()) {
            UserDeviceToken token = existingToken.get();
            token.updateLastUsed();
            token.setPlatform(platform);
            if (deviceInfo != null) {
                token.setDeviceInfo(deviceInfo);
            }

            UserDeviceToken saved = deviceTokenRepository.save(token);
            log.info("Updated device token for user {} on platform {}", userId, platform);
            return saved;

        } else {
            // Token mới -> tạo mới
            UserDeviceToken newToken = UserDeviceToken.builder()
                    .userId(userId)
                    .deviceToken(deviceToken)
                    .platform(platform)
                    .deviceInfo(deviceInfo)
                    .isActive(true)
                    .build();

            UserDeviceToken saved = deviceTokenRepository.save(newToken);
            log.info("Registered new device token for user {} on platform {}", userId, platform);
            return saved;
        }
    }


    @Transactional
    public UserDeviceToken registerDeviceToken(Long userId, String deviceToken, DevicePlatform platform) {
        return registerDeviceToken(userId, deviceToken, platform, null);
    }


    public List<UserDeviceToken> getUserActiveTokens(Long userId) {
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
    }


    public List<UserDeviceToken> getUserActiveTokensByPlatform(Long userId, DevicePlatform platform) {
        return deviceTokenRepository.findByUserIdAndPlatformAndIsActiveTrue(userId, platform);
    }


    @Transactional
    public void deactivateToken(Long userId, String deviceToken) {
        deviceTokenRepository.deactivateToken(userId, deviceToken);
        log.info("Deactivated device token for user {}", userId);
    }


    @Transactional
    public void deactivateAllUserTokens(Long userId) {
        deviceTokenRepository.deactivateAllUserTokens(userId);
        log.info("Deactivated all device tokens for user {}", userId);
    }


    public long countUserDevices(Long userId) {
        return deviceTokenRepository.countByUserIdAndIsActiveTrue(userId);
    }


    public boolean isTokenActive(Long userId, String deviceToken) {
        return deviceTokenRepository.existsByUserIdAndDeviceTokenAndIsActiveTrue(userId, deviceToken);
    }

    @Transactional
    public void cleanupOldInactiveTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        deviceTokenRepository.deleteInactiveTokensOlderThan(cutoffDate);
        log.info("Cleaned up inactive device tokens older than {}", cutoffDate);
    }
}

