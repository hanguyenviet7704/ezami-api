package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserVoucher;
import com.hth.udecareer.entities.Voucher;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.UserVoucherRepository;
import com.hth.udecareer.repository.VoucherRepository;
import com.hth.udecareer.service.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserVoucherServiceImpl implements UserVoucherService {

    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;

    @Override
    public PageResponse<UserVoucher> getMyVouchers(Principal principal, int page, int size) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<UserVoucher> vouchers = userVoucherRepository.findByUserIdOrderByReceivedAtDesc(user.getId(), pageable);

        return PageResponse.of(vouchers);
    }

    @Override
    public List<UserVoucher> getMyAvailableVouchers(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return userVoucherRepository.findAvailableByUserId(user.getId());
    }

    @Override
    @Transactional
    public UserVoucher claimVoucher(Principal principal, String voucherId) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Check if voucher exists
        Voucher voucher = voucherRepository.findByVoucherId(voucherId);
        if (voucher == null) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }

        // Check if already claimed
        if (userVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucherId)) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_CLAIMED);
        }

        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUserId(user.getId());
        userVoucher.setVoucherId(voucherId);
        userVoucher.setStatus("AVAILABLE");

        return userVoucherRepository.save(userVoucher);
    }

    @Override
    @Transactional
    public UserVoucher useVoucher(Principal principal, String voucherId) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(user.getId(), voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (!"AVAILABLE".equals(userVoucher.getStatus())) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
        }

        userVoucher.setStatus("USED");
        userVoucher.setUsedAt(LocalDateTime.now());

        return userVoucherRepository.save(userVoucher);
    }
}
