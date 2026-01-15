package com.hth.udecareer.model.response;

import com.hth.udecareer.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho chi tiết một referral
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDetailResponse {

    private Long referralId;

    private Long orderId;

    private String orderIdMasked;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private String customerEmailMasked;

    // Link information
    private Long linkId;

    private String linkUrl;

    private String prettyLink;

    // Order information
    private BigDecimal orderAmount;

    private String orderStatus;

    private LocalDateTime orderDate;

    private String productIds;

    private String productNames;

    private List<ProductInfo> products;

    // Commission information
    private String commissionType;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private CommissionStatus status;

    private String rejectionReason;

    // Visit tracking
    private Long visitId;

    private String deviceType;

    private String browser;

    private String referrerUrl;

    // Payout information
    private Long payoutId;

    private LocalDateTime paidAt;

    // Timestamps
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    // Additional metadata
    private String metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
    }
}

