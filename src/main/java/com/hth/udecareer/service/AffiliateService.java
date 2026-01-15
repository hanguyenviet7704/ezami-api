package com.hth.udecareer.service;

import com.hth.udecareer.enums.AffiliateStatus;
import com.hth.udecareer.model.request.AffiliateApproveRequest;
import com.hth.udecareer.model.request.AffiliatePaymentUpdateRequest;
import com.hth.udecareer.model.request.AffiliateRegisterRequest;
import com.hth.udecareer.model.request.AffiliateUpdateRequest;
import com.hth.udecareer.model.response.AffiliateRegisterResponse;
import com.hth.udecareer.model.response.AffiliateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface AffiliateService {
    AffiliateRegisterResponse register(AffiliateRegisterRequest request, Principal principal);

    AffiliateRegisterResponse approve(AffiliateApproveRequest request, String email);

    AffiliateRegisterResponse resubmit(AffiliateRegisterRequest request, String email);

    AffiliateRegisterResponse getStatusByEmail(String email);

    Page<AffiliateResponse> getAffiliatesByStatus(AffiliateStatus status, Pageable pageable, String email);

    AffiliateResponse getCurrentAffiliate(String email);

    AffiliateResponse updateCurrentAffiliate(String email, AffiliateUpdateRequest request);

    AffiliateResponse updatePaymentMethod(String email, AffiliatePaymentUpdateRequest request);
}
