package com.hth.udecareer.service;

import com.hth.udecareer.enums.CommissionStatus;
import com.hth.udecareer.enums.PeriodType;
import com.hth.udecareer.model.response.IncomeStatsResponse;
import com.hth.udecareer.model.response.PeriodIncomeStatsResponse;
import com.hth.udecareer.model.response.YearlyIncomeStatsResponse;
import com.hth.udecareer.repository.AffiliateReferralRepository;
import com.hth.udecareer.repository.AffiliateLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IncomeStatsService {

    private final AffiliateReferralRepository affiliateReferralRepository;
    private final AffiliateLinkRepository affiliateLinkRepository;

    public List<PeriodIncomeStatsResponse> calculateAggregateIncomeStats(
            Long affiliateId, PeriodType periodType, LocalDate fromDate, LocalDate toDate) {
        
        List<PeriodIncomeStatsResponse> stats = new ArrayList<>();
        
        switch (periodType) {
            case YEAR:
                stats = calculateYearlyStatsAggregate(affiliateId, fromDate.getYear(), toDate.getYear());
                break;
            case MONTH:
                stats = calculateMonthlyStats(affiliateId, fromDate, toDate);
                break;
            case WEEK:
                stats = calculateWeeklyStats(affiliateId, fromDate, toDate);
                break;
        }
        
        return stats;
    }

    private List<PeriodIncomeStatsResponse> calculateYearlyStatsAggregate(Long affiliateId, int fromYear, int toYear) {
        List<PeriodIncomeStatsResponse> yearlyStats = new ArrayList<>();
        
        for (int year = fromYear; year <= toYear; year++) {
            PeriodIncomeStatsResponse stats = calculateStatsForYear(affiliateId, year);
            stats.setPeriod(String.valueOf(year));
            stats.setPeriodLabel(String.valueOf(year));
            yearlyStats.add(stats);
        }
        
        return yearlyStats;
    }

    private List<PeriodIncomeStatsResponse> calculateMonthlyStats(Long affiliateId, LocalDate fromDate, LocalDate toDate) {
        List<PeriodIncomeStatsResponse> monthlyStats = new ArrayList<>();
        
        LocalDate current = fromDate.withDayOfMonth(1); // Đầu tháng
        LocalDate end = toDate.withDayOfMonth(1);
        
        while (!current.isAfter(end)) {
            PeriodIncomeStatsResponse stats = calculateStatsForYearMonth(affiliateId, current.getYear(), current.getMonthValue());
            stats.setPeriod(current.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            stats.setPeriodLabel(current.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
            monthlyStats.add(stats);
            
            current = current.plusMonths(1);
        }
        
        return monthlyStats;
    }

    private List<PeriodIncomeStatsResponse> calculateWeeklyStats(Long affiliateId, LocalDate fromDate, LocalDate toDate) {
        List<PeriodIncomeStatsResponse> weeklyStats = new ArrayList<>();
        
        // Tìm đầu tuần (Monday)
        LocalDate current = fromDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        
        while (!current.isAfter(toDate)) {
            LocalDate weekEnd = current.plusDays(6);
            
            PeriodIncomeStatsResponse stats = calculateStatsForDateRange(affiliateId, current, weekEnd);
            
            int weekOfYear = current.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            stats.setPeriod(current.getYear() + "-W" + String.format("%02d", weekOfYear));
            stats.setPeriodLabel("Week " + weekOfYear + ", " + current.getYear());
            weeklyStats.add(stats);
            
            current = current.plusWeeks(1);
        }
        
        return weeklyStats;
    }

    private PeriodIncomeStatsResponse calculateStatsForYear(Long affiliateId, int year) {
        // Tính tổng earnings từ AffiliateReferral (APPROVED status)
        BigDecimal totalEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearAndStatuses(
                affiliateId, year, Collections.singletonList(CommissionStatus.APPROVED));
        
        // Tính pending earnings (PENDING status)
        BigDecimal pendingEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearAndStatuses(
                affiliateId, year, Collections.singletonList(CommissionStatus.PENDING));
        
        // Tính paid earnings (PAID status)
        BigDecimal paidEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearAndStatuses(
                affiliateId, year, Collections.singletonList(CommissionStatus.PAID));
        
        // Tính total clicks từ affiliate links
        Long totalClicks = affiliateLinkRepository.sumTotalClicksByAffiliateAndYear(affiliateId, year);
        
        // Tính total orders từ affiliate referrals (APPROVED + PAID)
        Long totalOrders = affiliateReferralRepository.countOrdersByAffiliateAndYearAndStatuses(
                affiliateId, year, Arrays.asList(CommissionStatus.APPROVED, CommissionStatus.PAID));
        
        return buildStatsResponse(totalEarnings, pendingEarnings, paidEarnings, totalClicks, totalOrders);
    }

    private PeriodIncomeStatsResponse calculateStatsForYearMonth(Long affiliateId, int year, int month) {
        // Tính tổng earnings từ AffiliateReferral (APPROVED status)
        BigDecimal totalEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearMonthAndStatuses(
                affiliateId, year, month, Collections.singletonList(CommissionStatus.APPROVED));
        
        // Tính pending earnings (PENDING status)
        BigDecimal pendingEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearMonthAndStatuses(
                affiliateId, year, month, Collections.singletonList(CommissionStatus.PENDING));
        
        // Tính paid earnings (PAID status)
        BigDecimal paidEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearMonthAndStatuses(
                affiliateId, year, month, Collections.singletonList(CommissionStatus.PAID));
        
        // Tính total clicks từ affiliate links
        Long totalClicks = affiliateLinkRepository.sumTotalClicksByAffiliateAndYearMonth(affiliateId, year, month);
        
        // Tính total orders từ affiliate referrals (APPROVED + PAID)
        Long totalOrders = affiliateReferralRepository.countOrdersByAffiliateAndYearMonthAndStatuses(
                affiliateId, year, month, Arrays.asList(CommissionStatus.APPROVED, CommissionStatus.PAID));
        
        return buildStatsResponse(totalEarnings, pendingEarnings, paidEarnings, totalClicks, totalOrders);
    }

    private PeriodIncomeStatsResponse calculateStatsForDateRange(Long affiliateId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        // Tính tổng earnings từ AffiliateReferral (APPROVED status)
        BigDecimal totalEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Collections.singletonList(CommissionStatus.APPROVED));
        
        // Tính pending earnings (PENDING status)
        BigDecimal pendingEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Collections.singletonList(CommissionStatus.PENDING));
        
        // Tính paid earnings (PAID status)
        BigDecimal paidEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Collections.singletonList(CommissionStatus.PAID));
        
        // Tính total clicks từ affiliate links
        Long totalClicks = affiliateLinkRepository.sumTotalClicksInPeriodByAffiliate(affiliateId, start, end);
        
        // Tính total orders từ affiliate referrals (APPROVED + PAID)
        Long totalOrders = affiliateReferralRepository.countOrdersByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Arrays.asList(CommissionStatus.APPROVED, CommissionStatus.PAID));
        
        return buildStatsResponse(totalEarnings, pendingEarnings, paidEarnings, totalClicks, totalOrders);
    }

    private PeriodIncomeStatsResponse buildStatsResponse(BigDecimal totalEarnings, BigDecimal pendingEarnings, 
                                                        BigDecimal paidEarnings, Long totalClicks, Long totalOrders) {
        // Null safety
        totalEarnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
        pendingEarnings = pendingEarnings != null ? pendingEarnings : BigDecimal.ZERO;
        paidEarnings = paidEarnings != null ? paidEarnings : BigDecimal.ZERO;
        totalClicks = totalClicks != null ? totalClicks : 0L;
        totalOrders = totalOrders != null ? totalOrders : 0L;

        Double conversionRate = totalClicks > 0 ? (double) totalOrders / totalClicks * 100 : 0.0;

        return PeriodIncomeStatsResponse.builder()
                .totalEarnings(totalEarnings)
                .pendingEarnings(pendingEarnings)
                .paidEarnings(paidEarnings)
                .totalClicks(totalClicks)
                .totalOrders(totalOrders)
                .conversionRate(conversionRate)
                .build();
    }

    public List<YearlyIncomeStatsResponse> calculateYearlyIncomeStats(Long affiliateId, Integer fromYear, Integer toYear) {
        List<YearlyIncomeStatsResponse> yearlyStats = new ArrayList<>();
        
        for (int year = fromYear; year <= toYear; year++) {
            // Tính tổng earnings từ AffiliateReferral (APPROVED status) cho affiliate cụ thể
            BigDecimal totalEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearAndStatuses(
                    affiliateId, year, Collections.singletonList(CommissionStatus.APPROVED));
            if (totalEarnings == null) {
                totalEarnings = BigDecimal.ZERO;
            }

            // Tính pending earnings (PENDING status)
            BigDecimal pendingEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearAndStatuses(
                    affiliateId, year, Collections.singletonList(CommissionStatus.PENDING));
            if (pendingEarnings == null) {
                pendingEarnings = BigDecimal.ZERO;
            }

            // Tính paid earnings (PAID status)
            BigDecimal paidEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndYearAndStatuses(
                    affiliateId, year, Collections.singletonList(CommissionStatus.PAID));
            if (paidEarnings == null) {
                paidEarnings = BigDecimal.ZERO;
            }

            // Tính total clicks từ affiliate links của affiliate này trong năm
            Long totalClicks = affiliateLinkRepository.sumTotalClicksByAffiliateAndYear(affiliateId, year);
            
            // Tính total orders từ affiliate referrals của affiliate này (APPROVED + PAID)
            Long totalOrders = affiliateReferralRepository.countOrdersByAffiliateAndYearAndStatuses(
                    affiliateId, year, Arrays.asList(CommissionStatus.APPROVED, CommissionStatus.PAID));
            
            // Null safety
            totalClicks = totalClicks != null ? totalClicks : 0L;
            totalOrders = totalOrders != null ? totalOrders : 0L;

            Double conversionRate = totalClicks > 0 ? (double) totalOrders / totalClicks * 100 : 0.0;

            YearlyIncomeStatsResponse yearlyResponse = YearlyIncomeStatsResponse.builder()
                    .year(year)
                    .totalEarnings(totalEarnings)
                    .pendingEarnings(pendingEarnings)
                    .paidEarnings(paidEarnings)
                    .totalClicks(totalClicks)
                    .totalOrders(totalOrders)
                    .conversionRate(conversionRate)
                    .build();
            
            yearlyStats.add(yearlyResponse);
        }
        
        return yearlyStats;
    }

    public IncomeStatsResponse calculateIncomeStats(Long affiliateId, String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (period != null) {
            LocalDate now = LocalDate.now();
            switch (period.toLowerCase()) {
                case "week":
                    // Từ đầu tuần hiện tại (thứ 2) đến hiện tại
                    start = now.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                    end = now.atTime(LocalTime.MAX);
                    break;
                case "month":
                    // Từ đầu tháng hiện tại đến hiện tại
                    start = now.withDayOfMonth(1).atStartOfDay();
                    end = now.atTime(LocalTime.MAX);
                    break;
                case "year":
                    // Từ đầu năm hiện tại đến hiện tại
                    start = now.withDayOfYear(1).atStartOfDay();
                    end = now.atTime(LocalTime.MAX);
                    break;
                default:
                    break;
            }
        }

        // Custom date range có ưu tiên cao hơn period
        if (startDate != null && endDate != null) {
            start = startDate.atStartOfDay();
            end = endDate.atTime(LocalTime.MAX);
        }

        // Nếu không có period và custom date, lấy tất cả
        if (start == null || end == null) {
            start = LocalDateTime.of(2000, 1, 1, 0, 0);
            end = LocalDateTime.now();
        }

        // Tính tổng earnings từ AffiliateReferral (APPROVED status) cho affiliate cụ thể
        BigDecimal totalEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Collections.singletonList(CommissionStatus.APPROVED));
        if (totalEarnings == null) {
            totalEarnings = BigDecimal.ZERO;
        }

        // Tính pending earnings (PENDING status)
        BigDecimal pendingEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Collections.singletonList(CommissionStatus.PENDING));
        if (pendingEarnings == null) {
            pendingEarnings = BigDecimal.ZERO;
        }

        // Tính paid earnings (PAID status)
        BigDecimal paidEarnings = affiliateReferralRepository.sumCommissionByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Collections.singletonList(CommissionStatus.PAID));
        if (paidEarnings == null) {
            paidEarnings = BigDecimal.ZERO;
        }

        // Tính total clicks từ affiliate links của affiliate này (trong period dựa trên updatedAt)
        Long totalClicks = affiliateLinkRepository.sumTotalClicksInPeriodByAffiliate(affiliateId, start, end);
        
        // Tính total orders từ affiliate referrals của affiliate này (APPROVED + PAID)
        Long totalOrders = affiliateReferralRepository.countOrdersByAffiliateAndDateRangeAndStatuses(
                affiliateId, start, end, Arrays.asList(CommissionStatus.APPROVED, CommissionStatus.PAID));
        
        // Null safety
        totalClicks = totalClicks != null ? totalClicks : 0L;
        totalOrders = totalOrders != null ? totalOrders : 0L;

        Double conversionRate = totalClicks > 0 ? (double) totalOrders / totalClicks * 100 : 0.0;

        return IncomeStatsResponse.builder()
                .totalEarnings(totalEarnings)
                .pendingEarnings(pendingEarnings)
                .paidEarnings(paidEarnings)
                .totalClicks(totalClicks)
                .totalOrders(totalOrders)
                .conversionRate(conversionRate)
                .build();
    }
}