package com.hth.udecareer.service.Impl;

import com.cloudinary.utils.StringUtils;
import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.AffiliateStatus;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.mapper.AffiliateMapper;
import com.hth.udecareer.model.request.AffiliateApproveRequest;
import com.hth.udecareer.model.request.AffiliatePaymentUpdateRequest;
import com.hth.udecareer.model.request.AffiliateRegisterRequest;
import com.hth.udecareer.model.request.AffiliateUpdateRequest;
import com.hth.udecareer.model.response.AffiliateRegisterResponse;
import com.hth.udecareer.model.response.AffiliateResponse;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.AffiliateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AffiliateServiceImpl implements AffiliateService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateMapper affiliateMapper;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    @Transactional
    public AffiliateRegisterResponse register(AffiliateRegisterRequest request, Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Check if user already has an affiliate account (by userId - unique constraint)
        if (affiliateRepository.existsByUserId(user.getId())) {
            throw new AppException(ErrorCode.EMAIL_INFO_EXISTED);
        }

        // Check if email already exists (additional check for data consistency)
        if (affiliateRepository.existsByEmail(principal.getName())) {
            throw new AppException(ErrorCode.EMAIL_INFO_EXISTED);
        }

        validateUniqueInfo(request.getPhone(), request.getBankAccountNumber(), request.getBankName(), principal.getName());

        Affiliate affiliate = affiliateMapper.toAffiliate(request);
        affiliate.setEmail(user.getEmail());
        affiliate.setUserId(user.getId());
        affiliate.setStatus(AffiliateStatus.PENDING);
        affiliate.setAffiliateCode(generateAffiliateCode());
        affiliate.setRegisteredAt(LocalDateTime.now());

        affiliateRepository.save(affiliate);

        return affiliateMapper.toAffiliateRegisterResponse(affiliate);
    }

    @Override
    @Transactional
    public AffiliateRegisterResponse approve(AffiliateApproveRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        int admin = userMetaRepository.countAdminRole(user.getId());

        if (admin <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Affiliate affiliate = affiliateRepository.findById(request.getAffiliateId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (Boolean.TRUE.equals(request.getApproved())) {
            affiliate.setStatus(AffiliateStatus.ACTIVE);
            affiliate.setRejectionReason(null);
            affiliate.setApprovedAt(LocalDateTime.now());
            affiliate.setApprovedBy(user.getId());
        } else {
            if (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty()) {
                throw new AppException(ErrorCode.AFFILIATE_REJECT_REASON_REQUIRED);
            }
            affiliate.setStatus(AffiliateStatus.REJECTED);
            affiliate.setRejectionReason(request.getRejectReason().trim());
        }

        affiliateRepository.save(affiliate);

        return affiliateMapper.toAffiliateRegisterResponse(affiliate);
    }


    @Override
    @Transactional
    public AffiliateRegisterResponse resubmit(AffiliateRegisterRequest request, String email) {
        Affiliate affiliate = affiliateRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (affiliate.getStatus() != AffiliateStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_ACTION_STATUS_NOT_REJECTED);
        }

        validateUniqueInfo(request.getPhone(), request.getBankAccountNumber(), request.getBankName(), email);

        affiliateMapper.updateAffiliateFromRequest(affiliate, request);

        affiliate.setStatus(AffiliateStatus.PENDING);
        affiliate.setRejectionReason(null);

        affiliateRepository.save(affiliate);

        return affiliateMapper.toAffiliateRegisterResponse(affiliate);
    }

    @Override
    public AffiliateRegisterResponse getStatusByEmail(String email) {
        Affiliate affiliate = affiliateRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        AffiliateRegisterResponse response = new AffiliateRegisterResponse();
        response.setStatus(affiliate.getStatus());
        response.setRejectReason(affiliate.getRejectionReason());
        response.setAffiliateCode(affiliate.getAffiliateCode());
        return response;
    }

    @Override
    public Page<AffiliateResponse> getAffiliatesByStatus(AffiliateStatus status, Pageable pageable, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        int admin = userMetaRepository.countAdminRole(user.getId());

        if (admin <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Page<Affiliate> entities;

        if (status == null) {
            entities = affiliateRepository.findAll(pageable);
        } else {
            entities = affiliateRepository.findByStatus(status, pageable);
        }

        return entities.map(affiliateMapper::toAffiliateResponse);
    }

    @Override
    public AffiliateResponse getCurrentAffiliate(String email) {
        Affiliate affiliate = affiliateRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return affiliateMapper.toAffiliateResponse(affiliate);
    }

    @Override
    @Transactional
    public AffiliateResponse updateCurrentAffiliate(String email, AffiliateUpdateRequest request) {
        Affiliate affiliate = affiliateRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        validateUniqueInfo(request.getPhone(), request.getBankAccountNumber(), request.getBankName(), email);

        affiliateMapper.updateAffiliateFromUpdateRequest(affiliate, request);
        affiliateRepository.save(affiliate);

        return affiliateMapper.toAffiliateResponse(affiliate);
    }

    @Override
    @Transactional
    public AffiliateResponse updatePaymentMethod(String email, AffiliatePaymentUpdateRequest request) {
        Affiliate affiliate = affiliateRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        validateUniqueInfo(request.getPhone(), request.getBankAccountNumber(), request.getBankName(), email);

        affiliateMapper.updateAffiliatePaymentFromRequest(affiliate, request);
        affiliateRepository.save(affiliate);

        return affiliateMapper.toAffiliateResponse(affiliate);
    }

    private void validateUniqueInfo(String phone, String bankAccount, String bankName, String currentEmail) {

        if (StringUtils.isNotBlank(phone)) {
            boolean phoneExists = affiliateRepository.existsByPhoneAndEmailNot(
                    phone.trim(), currentEmail);
            if (phoneExists) {
                throw new AppException(ErrorCode.AFFILIATE_PHONE_IN_USE);
            }
        }

        if (StringUtils.isNotBlank(bankAccount)) {
            boolean bankExists = affiliateRepository.existsByBankAccountNumberAndBankNameAndEmailNot(
                    bankAccount.trim(),
                    StringUtils.isNotBlank(bankName) ? bankName.trim() : null,
                    currentEmail);
            if (bankExists) {
                throw new AppException(ErrorCode.AFFILIATE_BANK_ACCOUNT_IN_USE);
            }
        }
    }

    private String generateAffiliateCode() {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        while (affiliateRepository.findByAffiliateCode(code).isPresent()) {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
        return code;
    }
}
