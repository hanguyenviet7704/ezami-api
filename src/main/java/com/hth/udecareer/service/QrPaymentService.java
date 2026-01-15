package com.hth.udecareer.service;

import com.hth.udecareer.model.qr.QrGenerateResponse;
import com.hth.udecareer.model.request.VnpayPaymentRequest;

import java.util.Map;

public interface QrPaymentService {
    Map<String, Object> buyNowQr(VnpayPaymentRequest request, String username);
    Map<String, Object> checkoutQr(String username);
    QrGenerateResponse generateQr(String username, String amount, String message, long ttlSeconds);
}
