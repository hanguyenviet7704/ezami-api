package com.hth.udecareer.service;

import com.hth.udecareer.entities.AffiliateVisit;
import com.hth.udecareer.model.response.AffiliateClickReportResponse;
import com.hth.udecareer.repository.AffiliateVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateClickReportService {

    private final AffiliateVisitRepository affiliateVisitRepository;

    @Transactional(readOnly = true)
    public Page<AffiliateClickReportResponse> getClickReport(
            Long affiliateId,
            LocalDate startDate,
            LocalDate endDate,
            Long clickId,
            String subId,
            String clickArea,
            Pageable pageable) {

        log.info("Fetching click report for affiliate: {}, filters: startDate={}, endDate={}, clickId={}, subId={}, clickArea={}",
                affiliateId, startDate, endDate, clickId, subId, clickArea);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        // Xử lý empty string thành null để query hoạt động đúng
        String subIdFilter = (subId != null && !subId.trim().isEmpty()) ? subId.trim() : null;
        String clickAreaFilter = (clickArea != null && !clickArea.trim().isEmpty()) ? clickArea.trim() : null;

        Page<AffiliateVisit> visits = affiliateVisitRepository.findClicksWithFilters(
                affiliateId,
                startDateTime,
                endDateTime,
                clickId,
                subIdFilter,
                clickAreaFilter,
                pageable);

        return visits.map(this::mapToResponse);
    }

    private AffiliateClickReportResponse mapToResponse(AffiliateVisit visit) {
        return AffiliateClickReportResponse.builder()
                .visitId(visit.getId())
                .affiliateId(visit.getAffiliateId())
                .linkId(visit.getLinkId())
                .ipAddress(visit.getIpAddress())
                .userAgent(visit.getUserAgent())
                .referrerUrl(visit.getReferrerUrl())
                .landingUrl(visit.getLandingUrl())
                .campaign(visit.getCampaign())
                .medium(visit.getMedium())
                .source(visit.getSource())
                .deviceType(visit.getDeviceType() != null ? visit.getDeviceType().name().toLowerCase() : null)
                .browser(visit.getBrowser())
                .os(visit.getOs())
                .country(visit.getCountry())
                .city(visit.getCity())
                .isUnique(visit.getIsUnique())
                .isConverted(visit.getIsConverted())
                .convertedAt(visit.getConvertedAt())
                .referralId(visit.getReferralId())
                .sessionId(visit.getSessionId())
                .cookieValue(visit.getCookieValue())
                .expiresAt(visit.getExpiresAt())
                .createdAt(visit.getCreatedAt())
                .build();
    }
}

