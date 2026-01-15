package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateEarningResponse {
    private BigDecimal totalEarnings;      // APPROVED/UNPAID status
    private BigDecimal pendingEarnings;    // PENDING status
    private BigDecimal paidEarnings;       // PAID status
    private Long totalClicks;              // Total clicks from AffiliateLink
    private Long totalOrders;              // Total orders/conversions
    private Double conversionRate;         // (Orders / Clicks) * 100
}

