package com.hth.udecareer.model.qr;

import lombok.Data;

/**
 * Debug request for parsing / validating QR content.
 * Provide either 'qrContent' (raw EMV string) or 'transactionId' to use stored transaction.
 */
@Data
public class QrDebugRequest {
    private String qrContent;
    private String transactionId;
}
