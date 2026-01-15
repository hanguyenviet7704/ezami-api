package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.QrTransaction;
import com.hth.udecareer.model.request.VnpayPaymentRequest;
import com.hth.udecareer.service.OrderService;
import com.hth.udecareer.service.QRCodeService;
import com.hth.udecareer.service.QrPaymentService;
import com.hth.udecareer.service.QrTransactionService;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "QrPayment", description = "Thanh toán bằng mã QR")
public class QrPaymentController {

    private final QrPaymentService qrPaymentService;

    @PostMapping("/payment/qr/buy-now")
    @Operation(
            summary = "Thanh toán ngay bằng mã QR",
            description = """
                    Thanh toán ngay lập tức cho một quiz bằng mã QR.
                    
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> buyNowQr(@RequestBody VnpayPaymentRequest request,
                                      Principal principal,
                                      HttpServletRequest httpServletRequest) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String username = principal.getName();
        try {
            var resp = qrPaymentService.buyNowQr(request, username);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("QR buy-now error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @Operation(
            summary = "Thanh toán toán hết trong giỏ hàng bằng mã QR",
            description = """
                    Thanh toán toán hết trong giỏ hàng bằng mã QR
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/payment/qr/checkout")
    public ResponseEntity<?> checkoutQr(@RequestBody(required = false) Object body,
                                        Principal principal,
                                        HttpServletRequest httpServletRequest) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String username = principal.getName();
        try {
            var resp = qrPaymentService.checkoutQr(username);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("QR checkout error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
