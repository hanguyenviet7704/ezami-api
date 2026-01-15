package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.AffiliateApproveRequest;
import com.hth.udecareer.model.request.AffiliateRegisterRequest;
import com.hth.udecareer.model.response.AffiliateRegisterResponse;
import com.hth.udecareer.service.AffiliateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Affiliate", description = "Quản lý đăng ký và duyệt affiliate")
@SecurityRequirement(name = "bearerAuth")
public class AffiliateController {

    private final AffiliateService affiliateService;


    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đăng ký affiliate",
            description = "Đăng ký affiliate cho user hiện tại.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng ký affiliate thành công")
    })
    @PostMapping("/affiliates/register")
    public AffiliateRegisterResponse register(@Valid @RequestBody AffiliateRegisterRequest request,
                                              Principal principal) {
        return affiliateService.register(request, principal);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Duyệt affiliate",
            description = "Duyệt yêu cầu đăng ký affiliate.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyệt affiliate thành công")
    })
    @PutMapping("/affiliate/approve")
    public AffiliateRegisterResponse approve(@RequestBody AffiliateApproveRequest request, Principal principal) {
        return affiliateService.approve(request, principal.getName());
    }


    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/affiliate/status")
    public AffiliateRegisterResponse getStatus(Principal principal) {
        return affiliateService.getStatusByEmail(principal.getName());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gửi lại yêu cầu Affiliate (Khi bị từ chối)",
            description = "Cho phép user sửa thông tin và gửi lại yêu cầu xét duyệt. Chỉ gọi được khi trạng thái là REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật và gửi lại thành công"),
            @ApiResponse(responseCode = "400", description = "Lỗi: Hồ sơ không phải trạng thái REJECTED")
    })
    @PutMapping("/affiliates/resubmit")
    public AffiliateRegisterResponse resubmit(@Valid @RequestBody AffiliateRegisterRequest request,
                                              Principal principal) {
        return affiliateService.resubmit(request, principal.getName());
    }

}
