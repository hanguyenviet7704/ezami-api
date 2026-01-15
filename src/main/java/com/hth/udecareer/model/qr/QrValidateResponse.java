package com.hth.udecareer.model.qr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrValidateResponse {
    private boolean valid;
    private String transactionId;
    private String message;
}
