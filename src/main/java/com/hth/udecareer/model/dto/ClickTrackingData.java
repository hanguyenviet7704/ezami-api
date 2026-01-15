package com.hth.udecareer.model.dto;

import com.hth.udecareer.enums.DeviceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClickTrackingData {
    private Long linkId;
    private Long affiliateId;
    private String ipAddress;
    private String userAgent;
    private String referrerUrl;
    private String landingUrl;
    private String campaign;
    private String medium;
    private String source;
    private DeviceType deviceType;
    private String browser;
    private String os;
    private String country;
    private String city;
    private String sessionId;
    private String cookieValue;
    private LocalDateTime expiresAt;
}

