package com.hth.udecareer.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.hth.udecareer.entities.UserDeviceToken;
import com.hth.udecareer.model.dto.NotificationDTO;
import com.hth.udecareer.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final UserDeviceTokenRepository deviceTokenRepository;

    private boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }

    public String sendToDevice(String fcmToken, NotificationDTO notification) {
        if (!isFirebaseAvailable()) {
            log.warn("Firebase is not configured. Push notification skipped.");
            return null;
        }

        try {
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", notification.getId() != null ? notification.getId().toString() : "");
            data.put("title", notification.getTitle());
            data.put("message", notification.getContent());
            data.put("type", notification.getAction());

            if (notification.getObjectId() != null) {
                data.put("objectId", notification.getObjectId().toString());
            }
            if (notification.getRoute() != null) {
                data.put("route", notification.getRoute());
            }

            Notification fcmNotification = Notification.builder()
                    .setTitle(notification.getTitle())
                    .setBody(notification.getContent())
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(fcmNotification)
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setColor("#667eea")
                                    .setClickAction("OPEN_NOTIFICATION")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .setBadge(1)
                                    .setContentAvailable(true)
                                    .build())
                            .build())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Successfully sent push notification to device. MessageId: {}", messageId);
            return messageId;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to device: {}", e.getMessage(), e);

            handleFirebaseError(fcmToken, e);

            return null;
        }
    }


    public int sendToUser(Long userId, NotificationDTO notification) {
        // Lấy tất cả active tokens của user
        List<UserDeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId);
            return 0;
        }

        int successCount = 0;

        for (UserDeviceToken token : tokens) {
            String messageId = sendToDevice(token.getDeviceToken(), notification);
            if (messageId != null) {
                successCount++;
                // Update last used timestamp
                token.updateLastUsed();
                deviceTokenRepository.save(token);
            }
        }

        log.info("Sent push notification to user {} - Success: {}/{} devices",
                userId, successCount, tokens.size());

        return successCount;
    }

    public int sendToMultipleUsers(List<Long> userIds, NotificationDTO notification) {
        int totalSuccess = 0;

        for (Long userId : userIds) {
            totalSuccess += sendToUser(userId, notification);
        }

        return totalSuccess;
    }

    public String sendToTopic(String topic, NotificationDTO notification) {
        if (!isFirebaseAvailable()) {
            log.warn("Firebase is not configured. Push notification to topic skipped.");
            return null;
        }

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getContent())
                            .build())
                    .putData("type", notification.getAction())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Successfully sent push notification to topic: {}. MessageId: {}", topic, messageId);
            return messageId;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to topic {}: {}", topic, e.getMessage(), e);
            return null;
        }
    }


    private void handleFirebaseError(String fcmToken, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        // Các error codes cần deactivate token
        if (errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
            errorCode == MessagingErrorCode.UNREGISTERED) {

            log.warn("Invalid FCM token ({}), deactivating: {}", errorCode, fcmToken);

            List<UserDeviceToken> tokens = deviceTokenRepository.findAll().stream()
                    .filter(t -> t.getDeviceToken().equals(fcmToken))
                    .toList();

            tokens.forEach(token -> {
                token.deactivate();
                deviceTokenRepository.save(token);
            });
        }
    }
}

