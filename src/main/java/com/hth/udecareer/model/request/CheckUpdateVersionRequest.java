package com.hth.udecareer.model.request;

import lombok.Data;

@Data
public class CheckUpdateVersionRequest {
    private String appCode;

    private String os; // ios, android

    private String versionName;

    private String buildNumber;
}
