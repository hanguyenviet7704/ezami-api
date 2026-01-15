package com.hth.udecareer.model.qr;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class QrValidateRequest {
    @NotBlank
    private String qrContent;

    private String deviceInfo;
    private String location;
}
