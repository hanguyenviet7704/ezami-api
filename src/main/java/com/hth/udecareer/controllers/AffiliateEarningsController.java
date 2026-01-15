package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.enums.CommissionStatus;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.AffiliateEarningResponse;
import com.hth.udecareer.model.response.PagedResponse;
import com.hth.udecareer.model.response.ReferralDetailResponse;
import com.hth.udecareer.model.response.ReferralHistoryDto;
import com.hth.udecareer.model.response.ReferralStatsResponse;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.service.AffiliateReferralService;
import com.hth.udecareer.service.AffiliateStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Affiliate Earnings", description = "Quản lý thu nhập và hoa hồng")
@SecurityRequirement(name = "bearerAuth")
public class AffiliateEarningsController {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateStatsService affiliateStatsService;
    private final AffiliateReferralService affiliateReferralService;

    @Operation(summary = "Xem tổng quan thu nhập",
            description = "Lấy thống kê tổng quan về earnings, clicks và conversion rate")
    @GetMapping("/affiliates/earnings")
    public AffiliateEarningResponse getEarnings(Principal principal) {
        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Fetching earnings for affiliate: {}", affiliate.getId());
        return affiliateStatsService.getEarningsSummary(affiliate.getId());
    }

    @Operation(summary = "Xem lịch sử hoa hồng",
            description = "Lấy danh sách chi tiết các đơn hàng và hoa hồng với phân trang. Filter theo status: PENDING, APPROVED, PAID, REJECTED, CANCELLED")
    @GetMapping("/affiliates/me/referrals")
    public PagedResponse<ReferralHistoryDto> getMyReferrals(
            @RequestParam(required = false) CommissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        log.info("Fetching referrals for affiliate: {}, status: {}", affiliate.getId(), status);
        Page<ReferralHistoryDto> result = affiliateReferralService.getMyReferrals(affiliate.getId(), status, pageable);

        return PagedResponse.<ReferralHistoryDto>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .first(result.isFirst())
                .build();
    }

    @Operation(summary = "Xem chi tiết một referral",
            description = "Lấy thông tin chi tiết của một referral: products, customer, commission, tracking info")
    @GetMapping("/affiliates/me/referrals/{referralId}")
    public ReferralDetailResponse getReferralDetail(
            @PathVariable Long referralId,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Fetching referral detail: {} for affiliate: {}", referralId, affiliate.getId());
        return affiliateReferralService.getReferralDetail(referralId, affiliate.getId());
    }

    @Operation(summary = "Xem thống kê referrals",
            description = "Thống kê tổng quan: số lượng theo status, revenue, top products, monthly breakdown")
    @GetMapping("/affiliates/me/referrals/stats")
    public ReferralStatsResponse getReferralStats(Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Fetching referral stats for affiliate: {}", affiliate.getId());
        return affiliateReferralService.getReferralStats(affiliate.getId());
    }

    @Deprecated
    @Operation(summary = "[DEPRECATED] Xem lịch sử hoa hồng",
            description = "Use /affiliates/me/referrals instead")
    @GetMapping("/affiliates/referrals")
    public PagedResponse<ReferralHistoryDto> getReferrals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        log.info("Fetching referral history for affiliate: {}", affiliate.getId());
        Page<ReferralHistoryDto> result = affiliateStatsService.getReferralHistory(affiliate.getId(), pageable);

        return PagedResponse.<ReferralHistoryDto>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .first(result.isFirst())
                .build();
    }
}

