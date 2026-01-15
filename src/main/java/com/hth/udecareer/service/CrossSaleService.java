package com.hth.udecareer.service;

import com.hth.udecareer.enums.EcosystemApp;
import com.hth.udecareer.model.request.AppLogRequest;
import com.hth.udecareer.model.response.AppLogResponse;
import com.hth.udecareer.model.response.EcosystemAppResponse;

import java.security.Principal;
import java.util.List;

public interface CrossSaleService {
    List<EcosystemAppResponse> getApps();

    AppLogResponse saveLog(AppLogRequest appLogRequest, String email);
}
