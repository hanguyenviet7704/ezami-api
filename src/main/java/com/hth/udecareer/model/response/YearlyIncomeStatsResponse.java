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
public class YearlyIncomeStatsResponse {
    private Integer year;
    private BigDecimal totalEarnings;
    private BigDecimal pendingEarnings;
    private BigDecimal paidEarnings;
    private Long totalClicks;
    private Long totalOrders;
    private Double conversionRate;
}