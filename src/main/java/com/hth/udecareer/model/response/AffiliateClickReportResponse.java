package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateClickReportResponse {
    @JsonProperty("visit_id")
    private Long visitId;
    
    @JsonProperty("affiliate_id")
    private Long affiliateId;
    
    @JsonProperty("link_id")
    private Long linkId;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    @JsonProperty("referrer_url")
    private String referrerUrl;
    
    @JsonProperty("landing_url")
    private String landingUrl;
    
    private String campaign;
    private String medium;
    private String source;
    
    @JsonProperty("device_type")
    private String deviceType;
    
    private String browser;
    private String os;
    private String country;
    private String city;
    
    @JsonProperty("is_unique")
    private Boolean isUnique;
    
    @JsonProperty("is_converted")
    private Boolean isConverted;
    
    @JsonProperty("converted_at")
    private LocalDateTime convertedAt;
    
    @JsonProperty("referral_id")
    private Long referralId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("cookie_value")
    private String cookieValue;
    
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

