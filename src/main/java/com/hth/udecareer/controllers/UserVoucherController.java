package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.UserVoucher;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.service.UserVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "User Vouchers", description = "APIs for user voucher management (ez_user_vouchers)")
@SecurityRequirement(name = "bearerAuth")
public class UserVoucherController {

    private final UserVoucherService userVoucherService;

    @GetMapping("/user-vouchers/my")
    @Operation(summary = "Get my vouchers with pagination")
    public ResponseEntity<PageResponse<UserVoucher>> getMyVouchers(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userVoucherService.getMyVouchers(principal, page, size));
    }

    @GetMapping("/user-vouchers/available")
    @Operation(summary = "Get my available (unused) vouchers")
    public ResponseEntity<List<UserVoucher>> getMyAvailableVouchers(
            @Parameter(hidden = true) Principal principal
    ) {
        return ResponseEntity.ok(userVoucherService.getMyAvailableVouchers(principal));
    }

    @PostMapping("/user-vouchers/{voucherId}/claim")
    @Operation(summary = "Claim a voucher")
    public ResponseEntity<UserVoucher> claimVoucher(
            @Parameter(hidden = true) Principal principal,
            @PathVariable String voucherId
    ) {
        return ResponseEntity.ok(userVoucherService.claimVoucher(principal, voucherId));
    }

    @PostMapping("/user-vouchers/{voucherId}/use")
    @Operation(summary = "Mark a voucher as used")
    public ResponseEntity<UserVoucher> useVoucher(
            @Parameter(hidden = true) Principal principal,
            @PathVariable String voucherId
    ) {
        return ResponseEntity.ok(userVoucherService.useVoucher(principal, voucherId));
    }
}
