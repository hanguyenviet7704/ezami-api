package com.hth.udecareer.model.qr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrDebugResponse {
    private boolean crcOk;
    private Map<String, Object> parsed;
    private String extractedTransactionId;
    private String qrContent;
    private java.util.List<String> diagnostics;
}
