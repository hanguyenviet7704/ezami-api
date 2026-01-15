package com.hth.udecareer.service;

import com.hth.udecareer.model.dto.EzNotificationDto;
import com.hth.udecareer.model.response.PageResponse;

import java.security.Principal;

public interface EzNotificationService {
    PageResponse<EzNotificationDto> getMyNotifications(Principal principal, int page, int size, boolean unreadOnly);

    void markRead(Principal principal, Long notificationId);

    int markAllRead(Principal principal);
}

