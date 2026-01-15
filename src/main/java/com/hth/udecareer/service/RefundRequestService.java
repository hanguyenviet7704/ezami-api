package com.hth.udecareer.service;

import com.hth.udecareer.entities.RefundRequestEntity;
import com.hth.udecareer.model.request.RefundRequestCreateRequest;
import com.hth.udecareer.model.request.RefundRequestDecisionRequest;
import com.hth.udecareer.model.response.PageResponse;

import java.security.Principal;

public interface RefundRequestService {
    RefundRequestEntity createMyRequest(Principal principal, RefundRequestCreateRequest request);

    PageResponse<RefundRequestEntity> getMyRequests(Principal principal, int page, int size);

    PageResponse<RefundRequestEntity> getAllRequests(int page, int size);

    RefundRequestEntity approve(Long requestId, RefundRequestDecisionRequest body);

    RefundRequestEntity reject(Long requestId, RefundRequestDecisionRequest body);
}

