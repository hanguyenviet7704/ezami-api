package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.AffiliateClickReportResponse;
import com.hth.udecareer.model.response.PagedResponse;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.service.AffiliateClickReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Affiliate Click Report", description = "Báo cáo click cho affiliate")
@SecurityRequirement(name = "bearerAuth")
public class AffiliateClickReportController {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateClickReportService affiliateClickReportService;

    @Operation(summary = "Xem báo cáo click",
            description = "Lấy danh sách clicks với các filter: thời gian, click id, sub_id, khu vực click. " +
                    "Có phân trang và sắp xếp theo thời gian mới nhất.")
    @GetMapping("/affiliates/me/clicks")
    public PagedResponse<AffiliateClickReportResponse> getClickReport(
            @Parameter(description = "Ngày bắt đầu (format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Tìm kiếm theo click ID")
            @RequestParam(required = false) Long clickId,
            @Parameter(description = "Tìm kiếm theo sub_id (session_id hoặc cookie_value)")
            @RequestParam(required = false) String subId,
            @Parameter(description = "Tìm kiếm theo khu vực click (country hoặc city)")
            @RequestParam(required = false) String clickArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        log.info("Fetching click report for affiliate: {}, page: {}, size: {}", affiliate.getId(), page, size);

        Page<AffiliateClickReportResponse> result = affiliateClickReportService.getClickReport(
                affiliate.getId(),
                startDate,
                endDate,
                clickId,
                subId,
                clickArea,
                pageable);

        return PagedResponse.<AffiliateClickReportResponse>builder()
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

