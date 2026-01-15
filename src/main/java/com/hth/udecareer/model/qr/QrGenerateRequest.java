package com.hth.udecareer.model.qr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrGenerateRequest {
    @NotBlank
    @DecimalMin("1000")
    @DecimalMax("50000000")
    private String amount;

    @NotBlank
    private String message;
}
