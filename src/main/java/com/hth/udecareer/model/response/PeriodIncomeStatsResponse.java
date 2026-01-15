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
public class PeriodIncomeStatsResponse {
    private String period;           // "2024", "2024-12", "2024-W50"
    private String periodLabel;      // "2024", "December 2024", "Week 50, 2024"
    private BigDecimal totalEarnings;
    private BigDecimal pendingEarnings;
    private BigDecimal paidEarnings;
    private Long totalClicks;
    private Long totalOrders;
    private Double conversionRate;
}