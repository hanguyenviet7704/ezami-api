package com.hth.udecareer.model.qr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrGenerateOrderRequest {
    @NotNull
    private Long orderId;
    // Optional TTL seconds
    private Long ttlSeconds;
}
