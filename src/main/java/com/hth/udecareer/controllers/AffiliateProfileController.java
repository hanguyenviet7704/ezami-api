package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.AffiliatePaymentUpdateRequest;
import com.hth.udecareer.model.request.AffiliateUpdateRequest;
import com.hth.udecareer.model.response.AffiliateResponse;
import com.hth.udecareer.service.AffiliateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Affiliate Profile", description = "Quản lý thông tin cá nhân affiliate")
@SecurityRequirement(name = "bearerAuth")
public class AffiliateProfileController {

    private final AffiliateService affiliateService;

    @Operation(summary = "Lấy thông tin Affiliate của mình",
            description = "Xem chi tiết profile, số liệu thống kê cơ bản")
    @GetMapping("/affiliates/me")
    public AffiliateResponse getMe(Principal principal) {
        log.info("Affiliate {} viewing profile", principal.getName());
        return affiliateService.getCurrentAffiliate(principal.getName());
    }

    @Operation(summary = "Cập nhật thông tin Affiliate của mình",
            description = "Cập nhật thông tin cá nhân: website, social media, description...")
    @PutMapping("/affiliates/me")
    public AffiliateResponse updateMe(@Valid @RequestBody AffiliateUpdateRequest request,
                                      Principal principal) {
        log.info("Affiliate {} updating profile", principal.getName());
        return affiliateService.updateCurrentAffiliate(principal.getName(), request);
    }

    @Operation(summary = "Cập nhật phương thức thanh toán",
            description = "Cập nhật thông tin thanh toán: bank account, PayPal, etc.")
    @PutMapping("/affiliates/me/payment")
    public AffiliateResponse updatePayment(@Valid @RequestBody AffiliatePaymentUpdateRequest request,
                                           Principal principal) {
        log.info("Affiliate {} updating payment method", principal.getName());
        return affiliateService.updatePaymentMethod(principal.getName(), request);
    }
}

