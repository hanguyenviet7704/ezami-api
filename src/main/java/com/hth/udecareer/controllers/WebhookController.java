package com.hth.udecareer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.model.dto.RevenueCatWebhookDto;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.webhook.ContentAccessRequest;
import com.hth.udecareer.model.webhook.ContentAccessResponse;
import com.hth.udecareer.service.ContentAccessService;
import com.hth.udecareer.service.Impl.RevenueCatWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Hidden
public class WebhookController {

    private final RevenueCatWebhookService revenueCatWebhookService;
    private final ContentAccessService contentAccessService;
    private final ObjectMapper objectMapper;

    @PostMapping("/revenuecat")
    public ResponseEntity<?> handleRevenueCatWebhook(
            @RequestBody String payload)
            {
        log.info("Received RevenueCat webhook");
        try {
            RevenueCatWebhookDto.WebhookEvent event = objectMapper.readValue(
                    payload,
                    RevenueCatWebhookDto.WebhookEvent.class
            );
            log.info(payload);
            revenueCatWebhookService.processWebhookEvent(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/revenuecat/ping")
    public ApiResponse ping() {
        return ApiResponse.success("RevenueCat webhook endpoint is active");
    }

    @PostMapping("/content/checkPaid")
    public ResponseEntity<ApiResponse> checkContentAccess(@RequestBody ContentAccessRequest request) {
        try {
            ContentAccessResponse response = contentAccessService.hasAccess(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error checking content access: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}