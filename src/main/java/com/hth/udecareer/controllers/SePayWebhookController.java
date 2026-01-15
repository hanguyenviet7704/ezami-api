package com.hth.udecareer.controllers;

import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.webhook.SePayWebhookDto;
import com.hth.udecareer.service.SePayWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hooks")
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookController {
    private final SePayWebhookService sePayWebhookService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @org.springframework.beans.factory.annotation.Value("${SEPAY_WEBHOOK_API_KEY:}")
    private String sepayWebhookApiKey;

    @PostMapping("/sepay-payment")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody String payload, @RequestHeader(value = "X-SePay-Signature", required = false) String signature,
                                                @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                                @RequestHeader(value = "X-API-KEY", required = false) String xApiKey) {
        try {
            // If an API key is configured on server side, incoming requests must provide the same API key
            if (sepayWebhookApiKey != null && !sepayWebhookApiKey.isBlank()) {
                String incomingKey = null;
                if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                    String a = authorizationHeader.trim();
                    if (a.toLowerCase().startsWith("apikey ")) {
                        incomingKey = a.substring(7).trim();
                    } else {
                        // Some providers may use 'ApiKey ' or 'apikey'
                        incomingKey = a;
                    }
                }
                if (incomingKey == null && xApiKey != null && !xApiKey.isBlank()) incomingKey = xApiKey.trim();
                if (incomingKey == null || !sepayWebhookApiKey.equals(incomingKey)) {
                    log.warn("Invalid SePay webhook API key (missing or incorrect)");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(HttpStatus.UNAUTHORIZED, "Invalid API key"));
                }
            }
            // verify signature if configured
            if (!sePayWebhookService.verifyWebhookSignature(payload, signature)) {
                log.warn("Invalid SePay webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(HttpStatus.UNAUTHORIZED, "Invalid signature"));
            }
            SePayWebhookDto dto = objectMapper.readValue(payload, SePayWebhookDto.class);
            ApiResponse resp = sePayWebhookService.process(dto);
            return ResponseEntity.ok(resp);
        } catch (com.fasterxml.jackson.core.JsonProcessingException jex) {
            log.error("Invalid JSON payload for SePay webhook", jex);
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, "Invalid JSON"));
        } catch (Exception ex) {
            log.error("Error handling SePay webhook", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));
        }
    }
}
