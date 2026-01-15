package com.hth.udecareer.service;

import com.hth.udecareer.model.dto.response.AIAnalyticsResponse;

import java.security.Principal;

public interface AIAnalyticsService {
    AIAnalyticsResponse getStudentStats(Principal principal);
}
