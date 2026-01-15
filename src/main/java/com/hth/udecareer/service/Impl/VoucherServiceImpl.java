package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.Voucher;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.mapper.VoucherMapper;
import com.hth.udecareer.model.response.VoucherResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.VoucherRepository;
import com.hth.udecareer.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public List<VoucherResponse> getVoucherListOfUser(Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return voucherRepository.findVoucherListByUserId(user.getId());
    }

    @Override
    public VoucherResponse getVoucherDetail(String voucherId) {
        Voucher voucher = voucherRepository.findByVoucherId(voucherId);

        if (voucher == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        return voucherMapper.toVoucherResponse(voucher);
    }
}
