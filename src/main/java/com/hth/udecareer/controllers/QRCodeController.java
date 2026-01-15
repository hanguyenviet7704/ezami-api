package com.hth.udecareer.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.qr.QrDebugRequest;
import com.hth.udecareer.model.qr.QrDebugResponse;
import com.hth.udecareer.model.qr.QrGenerateRequest;
import com.hth.udecareer.model.qr.QrGenerateResponse;
import com.hth.udecareer.model.qr.QrValidateRequest;
import com.hth.udecareer.model.qr.QrValidateResponse;
import com.hth.udecareer.service.QRCodeService;
import com.hth.udecareer.service.QrTransactionService;
import com.hth.udecareer.service.SimpleRateLimiter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "QR code API", description = "Endpoints for generating and validating QR codes")
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final QrTransactionService qrTransactionService;
    private final com.hth.udecareer.service.QrPaymentService qrPaymentService;
    private final SimpleRateLimiter simpleRateLimiter;
    private final com.hth.udecareer.service.QrPaymentGrantService qrPaymentGrantService;
    private final com.hth.udecareer.service.QRCodeValidationService qrCodeValidationService;

    @Operation(summary = "Tạo mã QR thanh toán", description = """
            Tạo mã QR thanh toán dựa trên tài khoản đích mặc định cấu hình và số tiền (amount).
            API sẽ tạo một transaction tạm thời với TTL (mặc định 5 phút) và trả về transactionId cùng nội dung QR (TLV) đã được ký.

            Request body (QrGenerateRequest):
            - amount (String): Số tiền (chỉ số, ví dụ: 50000)
            - message (String, optional): Mô tả thanh toán

            Note: bankCode and bankAccount are no longer accepted from clients. The API will use configured defaults from application properties (app.qr.payment.bank-code, app.qr.payment.bank-account).

            Response (QrGenerateResponse):
            - transactionId: ID transaction (dùng cho truy vấn ảnh hoặc validate)
            - qrContent: Chuỗi QR (TLV) có thể dùng để hiển thị/scan
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo transaction & trả về QR content", content = @Content(mediaType = "application/json", schema = @Schema(implementation = QrGenerateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "qr/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrGenerateResponse generateQR(@Valid @RequestBody QrGenerateRequest request,
            @AuthenticationPrincipal UserDetails user) {
        // rate limiting by username or IP
        String key = user != null ? user.getUsername() : "anonymous";
        if (!simpleRateLimiter.tryConsume(key)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded");
        }

        String username = user != null ? user.getUsername() : "anonymous";
        return qrPaymentService.generateQr(username, request.getAmount(), request.getMessage(), 300L);
    }

    @GetMapping(value = "/qr/image/{transactionId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Lấy ảnh QR theo transactionId", description = "Generate image PNG of QR content previously generated for the given transaction ID. Requires authentication. Returns 404 if transaction not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ảnh QR được trả về (PNG)"),
            @ApiResponse(responseCode = "404", description = "Transaction không tồn tại", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public void generateImage(@PathVariable String transactionId, HttpServletResponse response) throws IOException {
        var opt = qrTransactionService.findByTransactionId(transactionId);
        if (opt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var tx = opt.get();
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        String qrContent = qrCodeService.buildQRContent(tx.getBankCode(), tx.getBankAccount(), tx.getAmount(),
                tx.getMessage(), tx.getTransactionId(), tx.getCreatedAt().getEpochSecond(),
                tx.getExpireAt().getEpochSecond(), tx.getSignatureKeyId());
        qrCodeService.generateImageFromQrContent(qrContent, response.getOutputStream());
    }

    @Operation(summary = "Validate QR content and mark transaction used", description = "Validate a provided QR content string (raw TLV or dataURI/image) and verify: CRC correctness, signature validity, transaction existence, expiry, and used state. If valid the transaction will be atomically marked as used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json", schema = @Schema(implementation = QrValidateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Missing or invalid payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Token invalid (if endpoint requires authentication)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(value = "/qr/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrValidateResponse validateQR(@Valid @RequestBody QrValidateRequest request,
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : "anonymous";
        return qrCodeValidationService.validateAndMarkQrContent(request, username);
    }

    @Operation(summary = "Debug parse QR content", description = "Provide debugging information for a QR content string. Accepts raw TLV or dataURI; returns parsed TLV, CRC status and an extracted transaction id if present. Use this to troubleshoot QR generation/validation issues.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parsed debug information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = QrDebugResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request (no input)"),
            @ApiResponse(responseCode = "404", description = "Transaction not found for provided transactionId"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/qr/debug/parse", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrDebugResponse debugParse(@Valid @RequestBody QrDebugRequest request) {
        String qrContent = qrCodeService.sanitizeQrContent(request.getQrContent());
        if ((qrContent == null || qrContent.isBlank())
                && (request.getTransactionId() == null || request.getTransactionId().isBlank())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide either qrContent or transactionId");
        }

        if ((qrContent == null || qrContent.isBlank()) && request.getTransactionId() != null
                && !request.getTransactionId().isBlank()) {
            var opt = qrTransactionService.findByTransactionId(request.getTransactionId());
            if (opt.isEmpty())
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Transaction not found");
            var tx = opt.get();
            qrContent = qrCodeService.buildQRContent(tx.getBankCode(), tx.getBankAccount(), tx.getAmount(),
                    tx.getMessage(), tx.getTransactionId(), tx.getCreatedAt().getEpochSecond(),
                    tx.getExpireAt().getEpochSecond(), tx.getSignatureKeyId());
        }

        try {
            var root = qrCodeService.parseTlv(qrContent);
            java.util.Map<String, Object> parsed = new java.util.HashMap<>();
            root.forEach(parsed::put);
            for (var entry : root.entrySet()) {
                String tag = entry.getKey();
                String value = entry.getValue();
                if (value != null && value.length() >= 4) {
                    // best-effort: parse sub TLVs
                    var sub = qrCodeService.parseTlv(value);
                    if (!sub.isEmpty())
                        parsed.put(tag + "_parsed", sub);
                }
            }

            boolean crcOk = qrCodeService.isValidEmvQrContent(qrContent);
            String extractedTx = qrCodeService.extractTransactionIdFromQrContent(qrContent);
            var diagnostics = qrCodeService.analyzeTlvDiagnostics(qrContent);

            return new QrDebugResponse(crcOk, parsed, extractedTx, qrContent, diagnostics);
        } catch (Exception ex) {
            log.error("[QRCodeController] debugParse error: {}", ex.getMessage(), ex);
            return new QrDebugResponse(false, java.util.Collections.emptyMap(), null, qrContent, java.util.List.of());
        }

    }

    @Operation(summary = "Get QR transaction metadata", description = "Retrieve basic transaction metadata by transactionId (message, amount, expiry, usage). Useful for UIs that need to show full message when QR does not include the full message). Note: This endpoint is read-only and returns non-sensitive info only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction metadata", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.qr.QrTransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized (if restricted)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping(value = "/qr/transaction/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<com.hth.udecareer.model.qr.QrTransactionResponse> getTransaction(
            @PathVariable String transactionId) {
        var opt = qrTransactionService.findByTransactionId(transactionId);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();
        var tx = opt.get();
        var resp = new com.hth.udecareer.model.qr.QrTransactionResponse(tx.getTransactionId(), tx.getAmount(),
                tx.getMessage(), tx.getExpireAt(), tx.isUsed());
        return ResponseEntity.ok(resp);
    }
}
