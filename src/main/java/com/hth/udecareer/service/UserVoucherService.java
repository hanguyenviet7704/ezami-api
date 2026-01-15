package com.hth.udecareer.service;

import com.hth.udecareer.entities.UserVoucher;
import com.hth.udecareer.model.response.PageResponse;

import java.security.Principal;
import java.util.List;

public interface UserVoucherService {

    PageResponse<UserVoucher> getMyVouchers(Principal principal, int page, int size);

    List<UserVoucher> getMyAvailableVouchers(Principal principal);

    UserVoucher claimVoucher(Principal principal, String voucherId);

    UserVoucher useVoucher(Principal principal, String voucherId);
}
