package com.hth.udecareer.service;

import com.hth.udecareer.model.qr.QrValidateRequest;
import com.hth.udecareer.model.qr.QrValidateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeValidationService {
    private final QRCodeService qrCodeService;
    private final QrTransactionService qrTransactionService;
    private final com.hth.udecareer.service.QrPaymentGrantService qrPaymentGrantService;

    public QrValidateResponse validateAndMarkQrContent(QrValidateRequest request, String username) {
        try {
            String qrContent = qrCodeService.sanitizeQrContent(request.getQrContent());
            log.debug("validateAndMarkQrContent: sanitized len={}, snippet='{}'", qrContent == null ? 0 : qrContent.length(), qrContent == null ? "" : qrContent.substring(0, Math.min(qrContent.length(), 80)));
            String txId = qrCodeService.extractTransactionIdFromQrContent(qrContent);
            if (txId == null) {
                var tlv = qrCodeService.parseTlv(qrContent);
                var tags = tlv.keySet();
                String tagsStr = String.join(",", tags);
                StringBuilder debugMsg = new StringBuilder();
                debugMsg.append("Invalid QR content: missing transaction id; found tags: ").append(tagsStr);
                if (tlv.containsKey("62")) {
                    var sub62 = qrCodeService.parseTlv(tlv.get("62"));
                    var keys = new java.util.ArrayList<String>(sub62.keySet());
                    debugMsg.append("; 62 subtags: ").append(String.join(",", keys));
                    if (sub62.containsKey("09")) debugMsg.append("; 62.09(sig): <REDACTED>");
                    if (sub62.containsKey("08")) {
                        String message = sub62.get("08");
                        if (message != null) {
                            String snippet = message.length() > 120 ? message.substring(0, 120) + "..." : message;
                            debugMsg.append("; 62.08(snippet): ").append(snippet);
                        }
                    }
                }
                var map = qrCodeService.parseTlv(qrContent);
                String amount = map.getOrDefault("54", null);
                String bankAccount = null;
                if (map.containsKey("38")) {
                    var maiMap = qrCodeService.parseTlv(map.get("38"));
                    if (maiMap.containsKey("01")) {
                        String maybeNested = maiMap.get("01");
                        var nested = qrCodeService.parseTlv(maybeNested);
                        if (nested.containsKey("01")) bankAccount = nested.get("01");
                        else bankAccount = maybeNested;
                    }
                }
                log.debug("Fallback attempt: amount={}, bankAccount={}", amount, bankAccount);
                if (amount != null && bankAccount != null) {
                    var optTx = qrTransactionService.findByBankAccountAndAmount(bankAccount, amount);
                    if (optTx.isPresent()) {
                        var candidate = optTx.get();
                        if (!candidate.isUsed() && candidate.getExpireAt().isAfter(java.time.Instant.now())) {
                            txId = candidate.getTransactionId();
                            log.debug("Fallback: matched transaction by bankAccount/amount, txId={}", txId);
                        }
                    }
                }
                if (txId == null) return new QrValidateResponse(false, null, debugMsg.toString());
            }

            var opt = qrTransactionService.findByTransactionId(txId);
            if (opt.isEmpty()) return new QrValidateResponse(false, null, "Transaction not found");
            var tx = opt.get();
            java.time.Instant now = java.time.Instant.now();
            if (tx.isUsed()) return new QrValidateResponse(false, txId, "Transaction already used");
            if (tx.getExpireAt().isBefore(now)) return new QrValidateResponse(false, txId, "Transaction expired");
            if (!qrCodeService.isValidEmvQrContent(qrContent)) return new QrValidateResponse(false, txId, "Invalid CRC");
            var parsedForSig = qrCodeService.parseTlv(qrContent);
            boolean hasSignature = false;
            if (parsedForSig.containsKey("62")) {
                var sub62 = qrCodeService.parseTlv(parsedForSig.get("62"));
                hasSignature = sub62.containsKey("09") && sub62.containsKey("10");
            }
            if (hasSignature && !qrCodeService.verifySignature(qrContent)) return new QrValidateResponse(false, txId, "Invalid signature");

            try {
                var parsed = qrCodeService.parseTlv(qrContent);
                if (parsed.containsKey("62") && parsed.get("62") != null && !parsed.get("62").isBlank()) {
                    var sub62 = qrCodeService.parseTlv(parsed.get("62"));
                    String qrMsg = sub62.get("08");
                    if (qrMsg != null && qrMsg.length() > 0 && tx.getMessage() != null && tx.getMessage().length() > 0) {
                        if (!tx.getMessage().startsWith(qrMsg)) return new QrValidateResponse(false, txId, "Message mismatch");
                    }
                }
            } catch (Exception ex) {
                log.debug("Error while validating QR message snippet: {}", ex.getMessage());
            }

            String usedBy = username == null ? "anonymous" : username;
            try {
                qrTransactionService.markUsed(tx.getTransactionId(), usedBy);
                try {
                    qrPaymentGrantService.processQrPaymentGrant(tx, usedBy);
                } catch (Exception exg) {
                    log.warn("Failed to process grant for QR tx {}: {}", tx.getTransactionId(), exg.getMessage());
                }
            } catch (IllegalStateException ex) {
                return new QrValidateResponse(false, txId, "Already used (race)");
            }
            return new QrValidateResponse(true, txId, "Valid");
        } catch (Exception ex) {
            log.error("[QRCodeValidationService] validateAndMarkQrContent error: {}", ex.getMessage(), ex);
            return new QrValidateResponse(false, null, "Internal Server Error");
        }
    }

}
