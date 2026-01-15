package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.CreatePrettyLinkRequest;
import com.hth.udecareer.model.request.UpdateLinkRequest;
import com.hth.udecareer.model.response.AffiliateLinkResponse;
import com.hth.udecareer.model.response.AffiliateLinkStatsResponse;
import com.hth.udecareer.model.response.PrettyLinkResponse;
import com.hth.udecareer.entities.AffiliateLink;
import com.hth.udecareer.repository.AffiliateLinkRepository;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.service.AffiliateLinkService;
import org.springframework.web.util.UriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Affiliate Links", description = "Quản lý affiliate links và tracking")
@SecurityRequirement(name = "bearerAuth")
public class AffiliateLinkController {

    private final AffiliateLinkService affiliateLinkService;
    private final AffiliateRepository affiliateRepository;
    private final AffiliateLinkRepository affiliateLinkRepository;

    @Operation(summary = "Tạo Pretty Link (Short URL)",
            description = "Tạo một liên kết rút gọn cho affiliate. Slug chỉ được chứa chữ cái, số và dấu gạch ngang.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo link thành công"),
            @ApiResponse(responseCode = "400", description = "Slug không hợp lệ hoặc đã tồn tại")
    })
    @PostMapping("/affiliates/create-link")
    public PrettyLinkResponse createPrettyLink(
            @Valid @RequestBody CreatePrettyLinkRequest request,
            Principal principal) {
        log.info("Creating pretty link for affiliate {}: {}", request.getAffiliateId(), request.getDesiredSlug());

        String prettyUrl = affiliateLinkService.createPrettyLink(
                request.getAffiliateId(),
                request.getTargetUrl(),
                request.getDesiredSlug()
        );

        // Get the created link from database (affiliateUrl already includes affiliateLinkId)
        AffiliateLink createdLink = affiliateLinkRepository.findByShortUrl(request.getDesiredSlug())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return PrettyLinkResponse.builder()
                .prettyUrl(prettyUrl)
                .slug(request.getDesiredSlug())
                .affiliateUrl(createdLink.getAffiliateUrl())
                .originalUrl(request.getTargetUrl())
                .affiliateId(request.getAffiliateId())
                .affiliateLinkId(createdLink.getId())
                .build();
    }

    @Operation(summary = "Lấy danh sách links của mình",
            description = "Xem tất cả affiliate links đã tạo với thông tin thống kê cơ bản")
    @GetMapping("/affiliates/me/links")
    public Page<AffiliateLinkResponse> getMyLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        log.info("Affiliate {} fetching links list", affiliate.getId());
        return affiliateLinkService.getMyLinks(affiliate.getId(), pageable);
    }

    @Operation(summary = "Xem chi tiết một link",
            description = "Lấy thông tin chi tiết của một affiliate link")
    @GetMapping("/affiliates/me/links/{linkId}")
    public AffiliateLinkResponse getLinkDetail(
            @PathVariable Long linkId,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Affiliate {} viewing link detail {}", affiliate.getId(), linkId);
        return affiliateLinkService.getLinkById(linkId, affiliate.getId());
    }

    @Operation(summary = "Cập nhật thông tin link",
            description = "Cập nhật campaign, medium, source của affiliate link để tracking tốt hơn")
    @PutMapping("/affiliates/me/links/{linkId}")
    public AffiliateLinkResponse updateLink(
            @PathVariable Long linkId,
            @Valid @RequestBody UpdateLinkRequest request,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Updating link {} for affiliate {}", linkId, affiliate.getId());
        return affiliateLinkService.updateLink(linkId, request, affiliate.getId());
    }

    @Operation(summary = "Vô hiệu hóa link",
            description = "Đánh dấu link không còn hoạt động. Link sẽ không redirect nữa.")
    @DeleteMapping("/affiliates/me/links/{linkId}")
    public void deactivateLink(
            @PathVariable Long linkId,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Deactivating link {} for affiliate {}", linkId, affiliate.getId());
        affiliateLinkService.deactivateLink(linkId, affiliate.getId());
    }

    @Operation(summary = "Xem thống kê chi tiết của link",
            description = "Lấy analytics: clicks, conversions, sources, devices, countries...")
    @GetMapping("/affiliates/me/links/{linkId}/stats")
    public AffiliateLinkStatsResponse getLinkStats(
            @PathVariable Long linkId,
            Principal principal) {

        Affiliate affiliate = affiliateRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("Fetching link stats {} for affiliate {}", linkId, affiliate.getId());
        return affiliateLinkService.getLinkStats(linkId, affiliate.getId());
    }
}

