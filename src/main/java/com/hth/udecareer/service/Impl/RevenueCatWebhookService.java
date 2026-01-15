package com.hth.udecareer.service.Impl;

import com.hth.udecareer.model.dto.RevenueCatWebhookDto;

public interface RevenueCatWebhookService {
    void processWebhookEvent(RevenueCatWebhookDto.WebhookEvent webhookEvent);
}
