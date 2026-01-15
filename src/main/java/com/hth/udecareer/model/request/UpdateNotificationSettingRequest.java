package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateNotificationSettingRequest {
    @NotNull(message = "NOTIFICATION_SETTING_REQUIRED")
    private Boolean allowPush;
}
