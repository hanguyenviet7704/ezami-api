package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RefundRequestCreateRequest {
    @NotNull(message = "VALIDATION_ERROR")
    private Long orderId;

    @NotBlank(message = "VALIDATION_ERROR")
    private String reason;

    private String description;
}
