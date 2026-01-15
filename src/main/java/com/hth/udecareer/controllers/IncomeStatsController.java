package com.hth.udecareer.controllers;

import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.PeriodType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.service.IncomeStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import com.hth.udecareer.model.response.IncomeStatsResponse;
import com.hth.udecareer.model.response.PeriodIncomeStatsResponse;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
@Tag(name = "Affiliate Earnings", description = "API for affiliate income statistics and reporting")
@SecurityRequirement(name = "bearerAuth")
public class IncomeStatsController {

    private final IncomeStatsService incomeStatsService;
    private final AffiliateRepository affiliateRepository;

    @GetMapping("/stats")
    @Operation(summary = "Get income statistics", description = "Retrieve income statistics for the authenticated affiliate for a specified period (week, month, year) or custom date range")
    public ResponseEntity<IncomeStatsResponse> getIncomeStats(
            @Parameter(description = "Period type: week, month, or year") @RequestParam(required = false) String period,
            @Parameter(description = "Start date in YYYY-MM-DD format") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date in YYYY-MM-DD format") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {

        // Get authenticated affiliate
        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Validation: nếu có startDate thì phải có endDate và ngược lại
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validation: startDate không được sau endDate
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        IncomeStatsResponse response = incomeStatsService.calculateIncomeStats(affiliate.getId(), period, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/aggregate")
    @Operation(summary = "Get aggregate income statistics", 
               description = "Retrieve income statistics aggregated by period (year/month/week) for comparison and analytics")
    public ResponseEntity<List<PeriodIncomeStatsResponse>> getAggregateIncomeStats(
            @Parameter(description = "Period type: YEAR, MONTH, or WEEK") 
            @RequestParam PeriodType period,
            @Parameter(description = "Start date in YYYY-MM-DD format") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in YYYY-MM-DD format") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal) {

        // Get authenticated affiliate
        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Validation: startDate không được sau endDate
        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<PeriodIncomeStatsResponse> response = incomeStatsService.calculateAggregateIncomeStats(
                affiliate.getId(), period, fromDate, toDate);
        return ResponseEntity.ok(response);
    }
}