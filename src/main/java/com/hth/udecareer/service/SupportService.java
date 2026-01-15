package com.hth.udecareer.service;

import com.hth.udecareer.model.request.SupportLogRequest;
import com.hth.udecareer.model.request.SupportProcessRequest;
import com.hth.udecareer.model.request.SupportRequestDto;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.SupportLogResponse;
import com.hth.udecareer.model.response.SupportResponse;

import java.security.Principal;
import java.util.LinkedHashMap;

public interface SupportService {
    LinkedHashMap<String, String> getSupportChannels();
    SupportLogResponse saveLog(SupportLogRequest request, Principal principal);
    void createSupportTicket(String email, SupportRequestDto request);
    PageResponse<SupportResponse> getSupportHistory(String email, int page, int size);

    SupportResponse getSupportDetail(Long supportId);
    SupportResponse processSupport(SupportProcessRequest request);
}
