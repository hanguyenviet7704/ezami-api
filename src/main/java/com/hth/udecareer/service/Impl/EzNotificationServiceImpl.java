package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.EzNotificationEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.EzNotificationDto;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.EzNotificationRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.EzNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class EzNotificationServiceImpl implements EzNotificationService {

    private final EzNotificationRepository ezNotificationRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<EzNotificationDto> getMyNotifications(Principal principal, int page, int size, boolean unreadOnly) {
        Long userId = getUserIdFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<EzNotificationEntity> notificationPage = unreadOnly
                ? ezNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                : ezNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        Page<EzNotificationDto> dtoPage = notificationPage.map(this::toDto);
        return PageResponse.of(dtoPage);
    }

    @Override
    @Transactional
    public void markRead(Principal principal, Long notificationId) {
        Long userId = getUserIdFromPrincipal(principal);
        EzNotificationEntity notification = ezNotificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        notification.setIsRead(true);
        ezNotificationRepository.save(notification);
    }

    @Override
    @Transactional
    public int markAllRead(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return ezNotificationRepository.markAllRead(userId);
    }

    private EzNotificationDto toDto(EzNotificationEntity e) {
        return EzNotificationDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .message(e.getMessage())
                .type(e.getType())
                .isRead(Boolean.TRUE.equals(e.getIsRead()))
                .referenceId(e.getReferenceId())
                .actionUrl(e.getActionUrl())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        return user.getId();
    }
}

