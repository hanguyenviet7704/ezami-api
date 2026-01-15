package com.hth.udecareer.service;

import com.hth.udecareer.entities.AffiliateLink;
import com.hth.udecareer.entities.AffiliateReferral;
import com.hth.udecareer.entities.AffiliateVisit;
import com.hth.udecareer.enums.CommissionStatus;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.ReferralDetailResponse;
import com.hth.udecareer.model.response.ReferralHistoryDto;
import com.hth.udecareer.model.response.ReferralStatsResponse;
import com.hth.udecareer.repository.AffiliateLinkRepository;
import com.hth.udecareer.repository.AffiliateReferralRepository;
import com.hth.udecareer.repository.AffiliateVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service quản lý referrals cho affiliate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateReferralService {

    private final AffiliateReferralRepository referralRepository;
    private final AffiliateLinkRepository linkRepository;
    private final AffiliateVisitRepository visitRepository;

    /**
     * Lấy danh sách referrals của affiliate với filter
     */
    @Transactional(readOnly = true)
    public Page<ReferralHistoryDto> getMyReferrals(Long affiliateId,
                                                     CommissionStatus status,
                                                     Pageable pageable) {
        log.info("Fetching referrals for affiliate: {}, status: {}", affiliateId, status);

        Page<AffiliateReferral> referrals;

        if (status != null) {
            referrals = referralRepository.findByAffiliateIdAndStatus(affiliateId, status, pageable);
        } else {
            referrals = referralRepository.findByAffiliateId(affiliateId, pageable);
        }

        return referrals.map(this::mapToReferralHistoryDto);
    }

    /**
     * Lấy chi tiết một referral
     */
    @Transactional(readOnly = true)
    public ReferralDetailResponse getReferralDetail(Long referralId, Long affiliateId) {
        log.info("Fetching referral detail: {} for affiliate: {}", referralId, affiliateId);

        AffiliateReferral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Referral not found"));

        // Verify ownership
        if (!referral.getAffiliateId().equals(affiliateId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "You don't have permission to view this referral");
        }

        return mapToReferralDetailResponse(referral);
    }

    /**
     * Lấy thống kê referrals
     */
    @Transactional(readOnly = true)
    public ReferralStatsResponse getReferralStats(Long affiliateId) {
        log.info("Fetching referral stats for affiliate: {}", affiliateId);

        List<AffiliateReferral> allReferrals = referralRepository.findAllByAffiliateId(affiliateId);

        // Count by status
        Map<CommissionStatus, Long> statusCounts = allReferrals.stream()
                .collect(Collectors.groupingBy(
                        AffiliateReferral::getStatus,
                        Collectors.counting()
                ));

        // Calculate totals
        BigDecimal totalRevenue = allReferrals.stream()
                .map(AffiliateReferral::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = allReferrals.stream()
                .map(AffiliateReferral::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingCommission = allReferrals.stream()
                .filter(r -> r.getStatus() == CommissionStatus.PENDING)
                .map(AffiliateReferral::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal approvedCommission = allReferrals.stream()
                .filter(r -> r.getStatus() == CommissionStatus.APPROVED)
                .map(AffiliateReferral::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidCommission = allReferrals.stream()
                .filter(r -> r.getStatus() == CommissionStatus.PAID)
                .map(AffiliateReferral::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate averages
        BigDecimal avgOrderValue = allReferrals.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(allReferrals.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgCommission = allReferrals.isEmpty() ? BigDecimal.ZERO :
                totalCommission.divide(BigDecimal.valueOf(allReferrals.size()), 2, RoundingMode.HALF_UP);

        // Top products
        List<ReferralStatsResponse.ProductStat> topProducts = getTopProducts(allReferrals);

        // Monthly stats
        List<ReferralStatsResponse.MonthlyStat> monthlyStats = getMonthlyStats(allReferrals);

        // Status distribution
        Map<String, Long> statusDistribution = statusCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue
                ));

        return ReferralStatsResponse.builder()
                .totalReferrals((long) allReferrals.size())
                .pendingCount(statusCounts.getOrDefault(CommissionStatus.PENDING, 0L))
                .approvedCount(statusCounts.getOrDefault(CommissionStatus.APPROVED, 0L))
                .paidCount(statusCounts.getOrDefault(CommissionStatus.PAID, 0L))
                .rejectedCount(statusCounts.getOrDefault(CommissionStatus.REJECTED, 0L))
                .cancelledCount(statusCounts.getOrDefault(CommissionStatus.CANCELLED, 0L))
                .totalRevenue(totalRevenue)
                .totalCommission(totalCommission)
                .pendingCommission(pendingCommission)
                .approvedCommission(approvedCommission)
                .paidCommission(paidCommission)
                .averageOrderValue(avgOrderValue)
                .averageCommission(avgCommission)
                .conversionRate(calculateConversionRate(affiliateId, allReferrals))
                .topProducts(topProducts)
                .monthlyStats(monthlyStats)
                .statusDistribution(statusDistribution)
                .build();
    }

    /**
     * Map entity to DTO
     */
    private ReferralHistoryDto mapToReferralHistoryDto(AffiliateReferral referral) {
        return ReferralHistoryDto.builder()
                .id(referral.getId())
                .createdAt(referral.getCreatedAt())
                .orderId(maskOrderId(referral.getOrderId()))
                .productNames(referral.getProductNames())
                .orderAmount(referral.getAmount())
                .commissionAmount(referral.getCommissionAmount())
                .status(referral.getStatus())
                .reason(referral.getRejectionReason())
                .build();
    }

    /**
     * Map entity to detail response
     */
    private ReferralDetailResponse mapToReferralDetailResponse(AffiliateReferral referral) {
        // Get link info if exists
        AffiliateLink link = null;
        if (referral.getLinkId() != null) {
            link = linkRepository.findById(referral.getLinkId()).orElse(null);
        }

        // Get visit info if exists
        AffiliateVisit visit = null;
        if (referral.getVisitId() != null) {
            visit = visitRepository.findById(referral.getVisitId()).orElse(null);
        }

        // Parse products
        List<ReferralDetailResponse.ProductInfo> products = parseProducts(
                referral.getProductIds(),
                referral.getProductNames(),
                referral.getAmount()
        );

        return ReferralDetailResponse.builder()
                .referralId(referral.getId())
                .orderId(referral.getOrderId())
                .orderIdMasked(maskOrderId(referral.getOrderId()))
                .customerId(referral.getCustomerId())
                .customerName(maskName(referral.getCustomerName()))
                .customerEmail(referral.getCustomerEmail())
                .customerEmailMasked(maskEmail(referral.getCustomerEmail()))
                .linkId(referral.getLinkId())
                .linkUrl(link != null ? link.getOriginalUrl() : null)
                .prettyLink(link != null ? link.getShortUrl() : null)
                .orderAmount(referral.getAmount())
                .orderStatus(referral.getOrderStatus())
                .orderDate(referral.getOrderDate())
                .productIds(referral.getProductIds())
                .productNames(referral.getProductNames())
                .products(products)
                .commissionType(referral.getCommissionType().name())
                .commissionRate(referral.getCommissionRate())
                .commissionAmount(referral.getCommissionAmount())
                .status(referral.getStatus())
                .rejectionReason(referral.getRejectionReason())
                .visitId(referral.getVisitId())
                .deviceType(visit != null && visit.getDeviceType() != null ? visit.getDeviceType().name() : null)
                .browser(visit != null ? visit.getBrowser() : null)
                .referrerUrl(visit != null ? visit.getReferrerUrl() : null)
                .payoutId(referral.getPayoutId())
                .paidAt(referral.getPaidAt())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .approvedAt(null) // DB field not available yet - requires migration
                .metadata(referral.getMetadata())
                .build();
    }

    /**
     * Get top products by order count
     */
    private List<ReferralStatsResponse.ProductStat> getTopProducts(List<AffiliateReferral> referrals) {
        Map<String, ReferralStatsResponse.ProductStat> productStatsMap = new HashMap<>();

        for (AffiliateReferral referral : referrals) {
            if (referral.getProductNames() == null) continue;

            String[] products = referral.getProductNames().split(",");
            for (String product : products) {
                product = product.trim();
                if (product.isEmpty()) continue;

                productStatsMap.computeIfAbsent(product, k ->
                    ReferralStatsResponse.ProductStat.builder()
                            .productName(k)
                            .orderCount(0L)
                            .totalRevenue(BigDecimal.ZERO)
                            .totalCommission(BigDecimal.ZERO)
                            .build()
                );

                ReferralStatsResponse.ProductStat stat = productStatsMap.get(product);
                stat.setOrderCount(stat.getOrderCount() + 1);
                stat.setTotalRevenue(stat.getTotalRevenue().add(referral.getAmount()));
                stat.setTotalCommission(stat.getTotalCommission().add(referral.getCommissionAmount()));
            }
        }

        return productStatsMap.values().stream()
                .sorted(Comparator.comparing(ReferralStatsResponse.ProductStat::getOrderCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get monthly statistics
     */
    private List<ReferralStatsResponse.MonthlyStat> getMonthlyStats(List<AffiliateReferral> referrals) {
        Map<String, ReferralStatsResponse.MonthlyStat> monthlyStatsMap = new HashMap<>();

        for (AffiliateReferral referral : referrals) {
            if (referral.getCreatedAt() == null) continue;

            String month = String.format("%d-%02d",
                    referral.getCreatedAt().getYear(),
                    referral.getCreatedAt().getMonthValue());

            monthlyStatsMap.computeIfAbsent(month, k ->
                    ReferralStatsResponse.MonthlyStat.builder()
                            .month(k)
                            .referralCount(0L)
                            .revenue(BigDecimal.ZERO)
                            .commission(BigDecimal.ZERO)
                            .build()
            );

            ReferralStatsResponse.MonthlyStat stat = monthlyStatsMap.get(month);
            stat.setReferralCount(stat.getReferralCount() + 1);
            stat.setRevenue(stat.getRevenue().add(referral.getAmount()));
            stat.setCommission(stat.getCommission().add(referral.getCommissionAmount()));
        }

        return monthlyStatsMap.values().stream()
                .sorted(Comparator.comparing(ReferralStatsResponse.MonthlyStat::getMonth).reversed())
                .limit(12)
                .collect(Collectors.toList());
    }

    /**
     * Parse product IDs and names into list
     * Distributes order amount evenly across products as estimate
     */
    private List<ReferralDetailResponse.ProductInfo> parseProducts(String productIds, String productNames, BigDecimal orderAmount) {
        if (productIds == null || productNames == null) {
            return Collections.emptyList();
        }

        String[] ids = productIds.split(",");
        String[] names = productNames.split(",");

        // Calculate estimated price per product (evenly distributed)
        BigDecimal pricePerProduct = BigDecimal.ZERO;
        if (orderAmount != null && ids.length > 0) {
            pricePerProduct = orderAmount.divide(
                    BigDecimal.valueOf(ids.length),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        List<ReferralDetailResponse.ProductInfo> products = new ArrayList<>();
        for (int i = 0; i < Math.min(ids.length, names.length); i++) {
            try {
                products.add(ReferralDetailResponse.ProductInfo.builder()
                        .productId(Long.parseLong(ids[i].trim()))
                        .productName(names[i].trim())
                        .price(pricePerProduct) // Estimated price (evenly distributed)
                        .quantity(1)
                        .build());
            } catch (NumberFormatException e) {
                log.warn("Invalid product ID: {}", ids[i]);
            }
        }

        return products;
    }

    /**
     * Mask order ID for privacy
     */
    private String maskOrderId(Long orderId) {
        if (orderId == null) {
            return "****";
        }
        String orderIdStr = orderId.toString();
        if (orderIdStr.length() <= 3) {
            return "****" + orderIdStr;
        }
        return "****" + orderIdStr.substring(orderIdStr.length() - 3);
    }

    /**
     * Mask customer name for privacy
     */
    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "***";
        }
        if (name.length() <= 3) {
            return name.charAt(0) + "***";
        }
        return name.substring(0, name.length() - 2) + "***";
    }

    /**
     * Mask email for privacy
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.com";
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 3) {
            return username.charAt(0) + "***@" + parts[1];
        }
        return username.substring(0, 3) + "***@" + parts[1];
    }

    /**
     * Calculate conversion rate from clicks
     */
    private double calculateConversionRate(Long affiliateId, List<AffiliateReferral> referrals) {
        try {
            // Get total clicks for this affiliate
            Long totalClicks = visitRepository.countByAffiliateId(affiliateId);

            if (totalClicks == null || totalClicks == 0) {
                return 0.0;
            }

            // Count successful conversions (approved or paid)
            long totalConversions = referrals.stream()
                    .filter(r -> r.getStatus() == CommissionStatus.APPROVED ||
                                 r.getStatus() == CommissionStatus.PAID)
                    .count();

            // Calculate rate as percentage
            return (totalConversions * 100.0 / totalClicks);
        } catch (Exception e) {
            log.warn("Error calculating conversion rate for affiliate {}: {}", affiliateId, e.getMessage());
            return 0.0;
        }
    }
}

