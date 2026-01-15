package com.hth.udecareer.model.qr;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QrGenerateResponse {
    private String transactionId;
    private String qrString; // raw EMV QR string
}
