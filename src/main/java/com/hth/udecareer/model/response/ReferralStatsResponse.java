package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO cho thống kê referrals
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatsResponse {

    // Overall statistics
    private Long totalReferrals;

    private Long pendingCount;

    private Long approvedCount;

    private Long paidCount;

    private Long rejectedCount;

    private Long cancelledCount;

    // Revenue statistics
    private BigDecimal totalRevenue;

    private BigDecimal totalCommission;

    private BigDecimal pendingCommission;

    private BigDecimal approvedCommission;

    private BigDecimal paidCommission;

    // Performance metrics
    private Double conversionRate;

    private BigDecimal averageOrderValue;

    private BigDecimal averageCommission;

    // Top products
    private List<ProductStat> topProducts;

    // Monthly breakdown
    private List<MonthlyStat> monthlyStats;

    // Status distribution
    private Map<String, Long> statusDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStat {
        private Long productId;
        private String productName;
        private Long orderCount;
        private BigDecimal totalRevenue;
        private BigDecimal totalCommission;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String month; // Format: YYYY-MM
        private Long referralCount;
        private BigDecimal revenue;
        private BigDecimal commission;
    }
}

