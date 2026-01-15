package com.hth.udecareer.service.impl;

import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.model.qr.QrGenerateResponse;
import com.hth.udecareer.model.request.VnpayPaymentRequest;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.OrderService;
import com.hth.udecareer.service.QRCodeService;
import com.hth.udecareer.service.QrPaymentService;
import com.hth.udecareer.service.QrTransactionService;
import com.hth.udecareer.entities.QrTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrPaymentServiceImpl implements QrPaymentService {

    private final OrderService orderService;
    private final QRCodeService qrCodeService;
    private final QrTransactionService qrTransactionService;
    private final UserRepository userRepository;

    @Value("${app.qr.payment.bank-code:vcb}")
    private String defaultBankCode;

    @Value("${app.qr.payment.bank-account:12345678}")
    private String defaultBankAccount;

    @Value("${app.qr.payment.ttl-seconds:300}")
    private long ttlSeconds;

    @Override
    public Map<String, Object> buyNowQr(VnpayPaymentRequest request, String username) {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        OrderEntity order = orderService.createBuyNowOrder(user.getId(), request.getCategoryCode(), request.getPlanCode());

        String amountStr = String.valueOf(order.getTotalAmount().longValue());
        String message = "Order#" + order.getId();
        Instant now = Instant.now();
        Instant expire = now.plusSeconds(ttlSeconds);
        QrTransaction tx = qrTransactionService.createTransaction(defaultBankCode, defaultBankAccount, amountStr, message, username, ttlSeconds, qrCodeService.getSigningKeyId());

        String qrContent = qrCodeService.buildQRContent(defaultBankCode, defaultBankAccount, amountStr, message, tx.getTransactionId(), now.getEpochSecond(), expire.getEpochSecond(), tx.getSignatureKeyId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("transactionId", tx.getTransactionId());
        resp.put("qrString", qrContent);
        resp.put("timestamp", System.currentTimeMillis());
        return resp;
    }

    @Override
    public Map<String, Object> checkoutQr(String username) {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        OrderEntity order = orderService.createCheckoutOrder(user.getId());
        orderService.updatePaymentMethod(order.getId(), "QRPAY");

        String amountStr = String.valueOf(order.getTotalAmount().longValue());
        String message = "Order#" + order.getId();
        Instant now = Instant.now();
        Instant expire = now.plusSeconds(ttlSeconds);
        QrTransaction tx = qrTransactionService.createTransaction(defaultBankCode, defaultBankAccount, amountStr, message, username, ttlSeconds, qrCodeService.getSigningKeyId());

        String qrContent = qrCodeService.buildQRContent(defaultBankCode, defaultBankAccount, amountStr, message, tx.getTransactionId(), now.getEpochSecond(), expire.getEpochSecond(), tx.getSignatureKeyId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("transactionId", tx.getTransactionId());
        resp.put("qrString", qrContent);
        resp.put("timestamp", System.currentTimeMillis());
        return resp;
    }

    @Override
    public QrGenerateResponse generateQr(String username, String amount, String message, long ttlSecondsLocal) {
        long ttl = ttlSecondsLocal > 0 ? ttlSecondsLocal : this.ttlSeconds;
        var tx = qrTransactionService.createTransaction(defaultBankCode, defaultBankAccount, amount, message, username, ttl, qrCodeService.getSigningKeyId());
        long now = Instant.now().getEpochSecond();
        long expiry = now + ttl;
        String qrContent = qrCodeService.buildQRContent(defaultBankCode, defaultBankAccount, amount, message, tx.getTransactionId(), now, expiry, tx.getSignatureKeyId());
        return new QrGenerateResponse(tx.getTransactionId(), qrContent);
    }
}
