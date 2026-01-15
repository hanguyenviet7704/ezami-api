package com.hth.udecareer.service;

import com.hth.udecareer.model.webhook.SePayWebhookDto;
import com.hth.udecareer.model.response.ApiResponse;

public interface SePayWebhookService {
    ApiResponse process(SePayWebhookDto dto);
    boolean verifyWebhookSignature(String payload, String signature);
}
