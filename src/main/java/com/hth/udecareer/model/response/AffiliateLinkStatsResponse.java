package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateLinkStatsResponse {
    private Long linkId;
    private String shortUrl;
    private String prettyUrl;
    private String originalUrl;

    // Overall stats
    private Integer totalClicks;
    private Integer uniqueClicks;
    private Integer totalConversions;
    private BigDecimal totalCommission;
    private BigDecimal conversionRate;

    // Time-based stats
    private Integer clicksToday;
    private Integer clicksThisWeek;
    private Integer clicksThisMonth;

    private Integer conversionsToday;
    private Integer conversionsThisWeek;
    private Integer conversionsThisMonth;

    // Top sources
    private List<SourceStat> topSources;

    // Device breakdown
    private DeviceStats deviceStats;

    // Geographic data
    private List<GeoStat> topCountries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceStat {
        private String source;
        private Integer clicks;
        private Integer conversions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStats {
        private Integer desktop;
        private Integer mobile;
        private Integer tablet;
        private Integer unknown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoStat {
        private String country;
        private Integer clicks;
        private Integer conversions;
    }
}

