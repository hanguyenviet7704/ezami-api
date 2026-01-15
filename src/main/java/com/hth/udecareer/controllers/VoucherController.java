package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.response.VoucherResponse;
import com.hth.udecareer.service.VoucherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Voucher Management")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/vouchers")
    public List<VoucherResponse> getVoucherListOfUser(Principal principal) {
        return voucherService.getVoucherListOfUser(principal);
    }

    @GetMapping("/voucher/{voucherId}/detail")
    public VoucherResponse getVoucherDetail(@PathVariable String voucherId) {
        return voucherService.getVoucherDetail(voucherId);
    }

}
