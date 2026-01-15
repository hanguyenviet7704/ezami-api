package com.hth.udecareer.service;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.hth.udecareer.entities.AppEntity;
import com.hth.udecareer.entities.VersionEntity;
import com.hth.udecareer.model.request.CheckUpdateVersionRequest;
import com.hth.udecareer.model.response.CheckUpdateVersionResponse;
import com.hth.udecareer.repository.AppRepository;
import com.hth.udecareer.repository.VersionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final VersionRepository versionRepository;
    private final AppRepository appRepository;

    public CheckUpdateVersionResponse checkUpdate(@NotNull final CheckUpdateVersionRequest request) {
        final String appCode = StringUtils.isBlank(request.getAppCode()) ? "ezami" : request.getAppCode().trim();
        final Optional<VersionEntity> versionOpt = versionRepository
                .getLatest(appCode, request.getOs())
                .stream()
                .findFirst();

        if (versionOpt.isPresent()) {
            final boolean hasNewVersion = hasNewVersion(request.getBuildNumber(), versionOpt.get().getBuildNumber());
            final Optional<AppEntity> appEntityOpt = appRepository.findEnableByAppCode(appCode);

            return CheckUpdateVersionResponse.
                    builder()
                    .haveNewVersion(hasNewVersion)
                    .forceDownload(versionOpt.get().isForceDownload())
                    .iosStoreUrl(appEntityOpt.map(AppEntity::getIosStoreUrl).orElse(""))
                    .androidStoreUrl(appEntityOpt.map(AppEntity::getAndroidStoreUrl).orElse(""))
                    .build();
        }
        return CheckUpdateVersionResponse.noNewVersion();
    }

    private static boolean hasNewVersion(final String currentBuildNumber,
                                         final String latestBuildNumber) {
        try {
            if (ObjectUtils.isEmpty(latestBuildNumber)
                || ObjectUtils.isEmpty(currentBuildNumber)) {
                return false;
            }
            final String[] currentArray = currentBuildNumber.split("\\.");
            final String[] latestArray = latestBuildNumber.split("\\.");

            if (currentArray.length == latestArray.length) {
                for (int i = 0; i < currentArray.length; i++) {
                    if (Integer.parseInt(currentArray[i]) > Integer.parseInt(latestArray[i])) {
                        return false;
                    } else if (Integer.parseInt(currentArray[i]) < Integer.parseInt(latestArray[i])) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return false;
    }
}
