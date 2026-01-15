package com.hth.udecareer.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckUpdateVersionResponse {
    private boolean haveNewVersion;

    private boolean forceDownload;

    private String iosStoreUrl;

    private String androidStoreUrl;

    public static CheckUpdateVersionResponse noNewVersion() {
        return builder()
                .haveNewVersion(false)
                .forceDownload(false)
                .build();
    }
}
