package com.hth.udecareer.service;

import com.hth.udecareer.entities.AffiliateReferral;
import com.hth.udecareer.enums.CommissionStatus;
import com.hth.udecareer.model.response.AffiliateEarningResponse;
import com.hth.udecareer.model.response.ReferralHistoryDto;
import com.hth.udecareer.repository.AffiliateReferralRepository;
import com.hth.udecareer.repository.AffiliateLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateStatsService {

    private final AffiliateReferralRepository affiliateReferralRepository;
    private final AffiliateLinkRepository affiliateLinkRepository;

    @Transactional(readOnly = true)
    public AffiliateEarningResponse getEarningsSummary(Long affiliateId) {
        log.info("Fetching earnings summary for affiliate: {}", affiliateId);

        // Total earnings (APPROVED status - ready but unpaid)
        BigDecimal totalEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndStatuses(
                affiliateId,
                Collections.singletonList(CommissionStatus.APPROVED)
        );

        // Pending earnings
        BigDecimal pendingEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndStatuses(
                affiliateId,
                Collections.singletonList(CommissionStatus.PENDING)
        );

        // Paid earnings
        BigDecimal paidEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndStatuses(
                affiliateId,
                Collections.singletonList(CommissionStatus.PAID)
        );

        // Total clicks from affiliate links
        Long totalClicks = affiliateLinkRepository.sumClicksByAffiliateId(affiliateId);

        // Total orders (conversions)
        Long totalOrders = affiliateReferralRepository.countOrdersByAffiliate(affiliateId);

        // Calculate conversion rate
        double conversionRate = 0.0;
        if (totalClicks != null && totalClicks > 0 && totalOrders != null) {
            conversionRate = (totalOrders.doubleValue() / totalClicks.doubleValue()) * 100;
            conversionRate = BigDecimal.valueOf(conversionRate)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return AffiliateEarningResponse.builder()
                .totalEarnings(totalEarnings != null ? totalEarnings : BigDecimal.ZERO)
                .pendingEarnings(pendingEarnings != null ? pendingEarnings : BigDecimal.ZERO)
                .paidEarnings(paidEarnings != null ? paidEarnings : BigDecimal.ZERO)
                .totalClicks(totalClicks != null ? totalClicks : 0L)
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .conversionRate(conversionRate)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ReferralHistoryDto> getReferralHistory(Long affiliateId, Pageable pageable) {
        log.info("Fetching referral history for affiliate: {}", affiliateId);

        Page<AffiliateReferral> referrals = affiliateReferralRepository.findByAffiliateId(
                affiliateId,
                pageable
        );

        return referrals.map(this::mapToReferralHistoryDto);
    }

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
}

