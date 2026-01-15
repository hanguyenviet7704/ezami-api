package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.AppLogEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.DeviceOS;
import com.hth.udecareer.enums.EcosystemApp;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.mapper.AppLogMapper;
import com.hth.udecareer.model.request.AppLogRequest;
import com.hth.udecareer.model.response.AppLogResponse;
import com.hth.udecareer.model.response.EcosystemAppResponse;
import com.hth.udecareer.repository.AppLogRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.CrossSaleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class CrossSaleServiceImpl implements CrossSaleService {

    private final AppLogRepository appLogRepository;
    private final UserRepository userRepository;
    private final AppLogMapper appLogMapper;

    @Override
    public List<EcosystemAppResponse> getApps() {
        return Arrays.stream(EcosystemApp.values())
                .map(app -> EcosystemAppResponse.builder()
                        .code(app.name())
                        .name(app.getName())
                        .description(app.getDescription())
                        .logoUrl(app.getLogoUrl())
                        .appStoreUrl(app.getAppStoreUrl())
                        .googlePlayUrl(app.getGooglePlayUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AppLogResponse saveLog(AppLogRequest appLogRequest, String email) {
        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));


        String appCode = appLogRequest.getAppCode().trim().toUpperCase();
        boolean validAppCode = Arrays.stream(EcosystemApp.values())
                .anyMatch(app -> app.name().equals(appCode));
        if (!validAppCode) {
            throw new AppException(ErrorCode.INVALID_APP_CODE);
        }

        DeviceOS deviceOS = DeviceOS.fromString(appLogRequest.getDeviceOs());
        if (deviceOS == DeviceOS.UNKNOWN) {
            throw new AppException(ErrorCode.INVALID_DEVICE_OS);
        }

        appLogRequest.setAppCode(appCode.trim().toUpperCase());
        appLogRequest.setDeviceOs(deviceOS.getValue().trim().toUpperCase());

        AppLogEntity log =  appLogMapper.toAppLogEntity(appLogRequest);
        log.setUserId(userId);

        appLogRepository.save(log);

        return AppLogResponse.builder().build();
    }

}
