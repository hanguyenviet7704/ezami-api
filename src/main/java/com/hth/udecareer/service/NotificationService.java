package com.hth.udecareer.service;

import com.hth.udecareer.config.TimezoneConfig;

import com.hth.udecareer.entities.NotificationEntity;
import com.hth.udecareer.entities.NotificationUserEntity;
import com.hth.udecareer.enums.NotificationType;
import com.hth.udecareer.enums.SourceObjectType;
import com.hth.udecareer.model.dto.NotificationDTO;
import com.hth.udecareer.model.dto.RealtimeNotificationDto;
import com.hth.udecareer.repository.NotificationRepository;
import com.hth.udecareer.repository.NotificationUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PushNotificationService pushNotificationService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationUserRepository notificationUserRepository,
                               @org.springframework.beans.factory.annotation.Autowired(required = false)
                               SimpMessagingTemplate messagingTemplate,
                               @org.springframework.beans.factory.annotation.Autowired(required = false)
                               PushNotificationService pushNotificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationUserRepository = notificationUserRepository;
        this.messagingTemplate = messagingTemplate;
        this.pushNotificationService = pushNotificationService;
    }

    @Transactional
    public void createNotification(Long userId, String title, String message, NotificationType type, String url) {
        createNotification(userId, title, message, type, url, null, null, null);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification(Long userId, String title, String message, NotificationType type, String url,
                                   Long objectId, Long srcUserId, SourceObjectType srcObjectType) {

        log.info("Creating notification for user {}: {} [objectId={}, srcUserId={}, srcObjectType={}]",
                userId, title, objectId, srcUserId, srcObjectType);
        log.info("🕰️ Current Vietnam time when creating notification: {}", TimezoneConfig.getCurrentVietnamTime());

        NotificationEntity notification;
        NotificationUserEntity notificationUser;

        try {
            notification = NotificationEntity.builder()
                    .title(title)
                    .content(message)
                    .action(type != null ? type.toString() : "SYSTEM_INFO")
                    .route(url)
                    .objectId(objectId)
                    .srcUserId(srcUserId)
                    .srcObjectType(srcObjectType != null ? srcObjectType.getValue() : null)
                    .feedId(null)
                    .build();

            notification = notificationRepository.save(notification);
            log.info("Notification content saved to wp_fcom_notifications with ID: {}", notification.getId());

            notificationUser = NotificationUserEntity.builder()
                    .userId(userId)
                    .objectId(notification.getId())
                    .isRead(0)
                    .objectType("notification")
                    .notificationType("web")
                    .build();

            notificationUser = notificationUserRepository.save(notificationUser);
            log.info("Notification user mapping saved to wp_fcom_notification_users with ID: {}", notificationUser.getId());

        } catch (Exception e) {
            log.error("Failed to save notification to database for user {}: {}", userId, e.getMessage(), e);
            // Don't throw exception to prevent parent transaction rollback
            return;
        }

        sendWebSocketNotification(userId, notification);

        sendPushNotification(userId, notification);
    }

    private void sendWebSocketNotification(Long userId, NotificationEntity notification) {
            if (messagingTemplate != null) {
                try {
                    // Tạo payload cho realtime notification
                    RealtimeNotificationDto realtimeDto = RealtimeNotificationDto.builder()
                            .id(notification.getId())
                            .title(notification.getTitle())
                            .message(notification.getContent())
                            .type(notification.getAction())
                            .actionUrl(notification.getRoute())
                            .createdAt(notification.getCreatedAt())
                            .build();

                    // Gửi thông báo riêng cho user thông qua WebSocket
                    String destination = "/topic/notifications/" + userId;
                    messagingTemplate.convertAndSend(destination, realtimeDto);

                log.info("Sent realtime notification to user {} via WebSocket: {}", userId, notification.getTitle());

                } catch (Exception e) {
                    log.error("Failed to send realtime notification to user {}: {}", userId, e.getMessage(), e);
                }
            } else {
                log.warn("SimpMessagingTemplate is not available. Skipping WebSocket notification.");
            }
    }

    private void sendPushNotification(Long userId, NotificationEntity notification) {
        if (pushNotificationService != null) {
            try {
                NotificationDTO pushNotification = NotificationDTO.builder()
                        .id(notification.getId())
                        .title(notification.getTitle())
                        .content(notification.getContent())
                        .action(notification.getAction())
                        .route(notification.getRoute())
                        .objectId(notification.getObjectId())
                        .srcUserId(notification.getSrcUserId())
                        .srcObjectType(notification.getSrcObjectType())
                        .feedId(notification.getFeedId())
                        .build();

                int devicesNotified = pushNotificationService.sendToUser(userId, pushNotification);

                if (devicesNotified > 0) {
                    log.info("Sent push notification to user {} - {} devices notified: {}",
                            userId, devicesNotified, notification.getTitle());
                } else {
                    log.warn("No devices received push notification for user {}: {}",
                            userId, notification.getTitle());
                }

            } catch (Exception e) {
                log.error("Failed to send push notification to user {}: {}", userId, e.getMessage(), e);
            }
        } else {
            log.warn("PushNotificationService is not available. Skipping push notifications.");
        }
    }

    @Transactional
    public boolean markNotificationAsRead(Long userId, Long notificationId) {
        try {
            var notificationUserOpt = notificationUserRepository.findByUserIdAndObjectId(userId, notificationId);
            if (notificationUserOpt.isPresent()) {
                NotificationUserEntity notificationUser = notificationUserOpt.get();
                notificationUser.markAsRead();
                notificationUser.setUpdatedAt(TimezoneConfig.getCurrentVietnamTime());
                notificationUserRepository.save(notificationUser);
                log.info("Marked notification {} as read for user {}", notificationId, userId);
                return true;
            } else {
                log.warn("Notification {} not found for user {}", notificationId, userId);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to mark notification {} as read for user {}: {}", notificationId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public int markAllNotificationsAsRead(Long userId) {
        try {
            int updatedCount = notificationUserRepository.markAllAsReadByUserId(userId);
            log.info("Marked {} notifications as read for user {}", updatedCount, userId);
            return updatedCount;
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for user {}: {}", userId, e.getMessage(), e);
            return 0;
        }
    }

    @Transactional
    public boolean deleteUserNotification(Long userId, Long notificationId) {
        try {
            var notificationUserOpt = notificationUserRepository.findByUserIdAndObjectId(userId, notificationId);
            if (notificationUserOpt.isPresent()) {
                notificationUserRepository.delete(notificationUserOpt.get());
                log.info("Deleted notification {} for user {}", notificationId, userId);
                return true;
            } else {
                log.warn("Notification {} not found for user {}", notificationId, userId);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to delete notification {} for user {}: {}", notificationId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public void deleteAllUserNotifications(Long userId) {
        try {
            notificationUserRepository.deleteByUserId(userId);
            log.info("Deleted all notifications for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete all notifications for user {}: {}", userId, e.getMessage(), e);
        }
    }
}