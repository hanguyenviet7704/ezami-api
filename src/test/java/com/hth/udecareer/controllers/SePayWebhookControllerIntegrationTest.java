package com.hth.udecareer.controllers;

// SePay webhook integration test uses real SpringBoot context and verifies HMAC signature.
import com.hth.udecareer.model.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.TestPropertySource(properties = {"sepay.webhookSecret=test-secret-123456", "SEPAY_WEBHOOK_API_KEY=test-api-key"})
public class SePayWebhookControllerIntegrationTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testSePayController_verifySignature() throws Exception {
        String secret = "test-secret-123456";
        String payload = "{\"id\":999,\"transferAmount\":1000,\"transferType\":\"in\"}";

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        byte[] sig = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String base64 = java.util.Base64.getEncoder().encodeToString(sig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-SePay-Signature", base64);
        // add valid API key
        headers.add("Authorization", "Apikey test-api-key");

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<ApiResponse> resp = template.postForEntity("/hooks/sepay-payment", entity, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());

        // invalid signature
        headers.set("X-SePay-Signature", "bad");
        entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp2 = template.postForEntity("/hooks/sepay-payment", entity, String.class);
        assertEquals(401, resp2.getStatusCodeValue());
    }
}
