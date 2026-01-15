package com.hth.udecareer.service;

import com.hth.udecareer.model.response.VoucherResponse;

import java.security.Principal;
import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getVoucherListOfUser(Principal principal);
    VoucherResponse getVoucherDetail(String voucherId);
}
