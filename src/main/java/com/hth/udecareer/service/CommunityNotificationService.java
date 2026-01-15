package com.hth.udecareer.service;

import com.hth.udecareer.entities.NotificationEntity;
import com.hth.udecareer.entities.NotificationUserEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.XProfileEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.NotificationWithStatusDTO;
import com.hth.udecareer.model.response.CommunityNotificationResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UnreadCountResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.repository.NotificationRepository;
import com.hth.udecareer.repository.NotificationUserRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.XProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final XProfileRepository xProfileRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    /**
     * Get notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<CommunityNotificationResponse> getNotifications(Long userId, int page, int size, Boolean unreadOnly) {
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationWithStatusDTO> notifications;
        if (unreadOnly != null && unreadOnly) {
            notifications = notificationRepository.findUnreadNotificationDTOsForUser(userId, pageable);
        } else {
            notifications = notificationRepository.findNotificationDTOsForUser(userId, pageable);
        }

        List<CommunityNotificationResponse> items = notifications.getContent().stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());

        return PageResponse.<CommunityNotificationResponse>builder()
                .content(items)
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .hasNext(notifications.hasNext())
                .hasPrevious(notifications.hasPrevious())
                .first(notifications.isFirst())
                .last(notifications.isLast())
                .build();
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public CommunityNotificationResponse markAsRead(Long userId, Long notificationId) {
        NotificationUserEntity notificationUser = notificationUserRepository
                .findByUserIdAndObjectId(userId, notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));

        notificationUser.markAsRead();
        notificationUserRepository.save(notificationUser);

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));

        log.info("Notification {} marked as read by user {}", notificationId, userId);

        return mapNotificationEntityToResponse(notification, true, notificationUser.getUpdatedAt());
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int updated = notificationUserRepository.markAllAsReadByUserId(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
        return updated;
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        Long count = notificationUserRepository.countUnreadByUserId(userId);
        return UnreadCountResponse.builder()
                .unreadCount(count != null ? count : 0L)
                .build();
    }

    // ============= HELPER METHODS =============

    private CommunityNotificationResponse mapToNotificationResponse(NotificationWithStatusDTO dto) {
        XProfileResponse srcUser = null;
        if (dto.getSrcUserId() != null) {
            srcUser = getXProfile(dto.getSrcUserId());
        }

        String route = null;
        if (dto.getRoute() != null) {
            route = dto.getRoute().toString();
        }

        return CommunityNotificationResponse.builder()
                .id(dto.getId())
                .action(dto.getAction())
                .title(dto.getTitle())
                .content(dto.getContent())
                .route(route)
                .isRead(dto.isRead())
                .feedId(dto.getFeedId())
                .srcUserId(dto.getSrcUserId())
                .srcUser(srcUser)
                .createdAt(dto.getCreatedAt())
                .readAt(dto.getReadAt())
                .build();
    }

    private CommunityNotificationResponse mapNotificationEntityToResponse(NotificationEntity entity,
            boolean isRead, java.time.LocalDateTime readAt) {
        XProfileResponse srcUser = null;
        if (entity.getSrcUserId() != null) {
            srcUser = getXProfile(entity.getSrcUserId());
        }

        return CommunityNotificationResponse.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .title(entity.getTitle())
                .content(entity.getContent())
                .route(entity.getRoute())
                .isRead(isRead)
                .feedId(entity.getFeedId())
                .srcUserId(entity.getSrcUserId())
                .srcUser(srcUser)
                .createdAt(entity.getCreatedAt())
                .readAt(readAt)
                .build();
    }

    private XProfileResponse getXProfile(Long userId) {
        Optional<XProfileEntity> xProfileOpt = xProfileRepository.findByUserId(userId);
        Optional<User> userOpt = userRepository.findById(userId);

        String finalAvatarFromMeta = userMetaRepository.findByUserIdAndMetaKey(userId, "wpcf-avatar")
                .map(meta -> meta.getMetaValue())
                .orElse(null);

        if (xProfileOpt.isPresent()) {
            XProfileEntity xp = xProfileOpt.get();
            String finalAvatar = StringUtils.isNotBlank(xp.getAvatar()) ? xp.getAvatar() : finalAvatarFromMeta;
            String fullName = userOpt.map(User::getDisplayName).orElse(null);

            return XProfileResponse.builder()
                    .userId(xp.getUserId())
                    .username(xp.getUsername())
                    .displayName(xp.getDisplayName())
                    .fullName(fullName)
                    .avatar(finalAvatar)
                    .totalPoints(xp.getTotalPoints())
                    .isVerified(xp.getIsVerified() != null && xp.getIsVerified() == 1)
                    .build();
        } else {
            return userOpt.map(user -> XProfileResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .fullName(user.getDisplayName())
                    .avatar(finalAvatarFromMeta)
                    .totalPoints(0)
                    .isVerified(false)
                    .build())
                    .orElse(null);
        }
    }
}
