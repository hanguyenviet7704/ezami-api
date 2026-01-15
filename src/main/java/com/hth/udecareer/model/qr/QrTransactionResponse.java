package com.hth.udecareer.model.qr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrTransactionResponse {
    private String transactionId;
    private String amount;
    private String message;
    private Instant expireAt;
    private boolean used;
}
