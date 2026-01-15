package com.hth.udecareer.model.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private String status;
    private String paymentUrl; // optional if vnpay enabled
    private Long orderId;      // created order id
    private BigDecimal orderAmount;
    private Long timestamp;
}
