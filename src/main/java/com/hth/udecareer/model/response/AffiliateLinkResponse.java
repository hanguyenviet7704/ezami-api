package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateLinkResponse {
    private Long linkId;
    private Long affiliateId;
    private String originalUrl;
    private String affiliateUrl;
    private String shortUrl;
    private String prettyUrl;
    private String campaign;
    private String medium;
    private String source;
    private String linkType;
    private Boolean isActive;

    // Statistics
    private Integer totalClicks;
    private Integer uniqueClicks;
    private Integer totalConversions;
    private BigDecimal totalCommission;
    private BigDecimal conversionRate; // (conversions / clicks) * 100

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime expiresAt;
}


