package com.hth.udecareer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.model.qr.QrDebugRequest;
import com.hth.udecareer.model.qr.QrDebugResponse;
import com.hth.udecareer.model.qr.QrValidateRequest;
import com.hth.udecareer.model.qr.QrValidateResponse;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.service.QRCodeService;
import com.hth.udecareer.service.QRCodeValidationService;
import com.hth.udecareer.service.QrPaymentGrantService;
import com.hth.udecareer.service.QrPaymentService;
import com.hth.udecareer.service.QrTransactionService;
import com.hth.udecareer.service.SimpleRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com")
class QRCodeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private SimpleRateLimiter simpleRateLimiter;

    @Autowired
    private QrPaymentGrantService qrPaymentGrantService;

    @Autowired
    private QrTransactionService qrTransactionService;

    @Autowired
    private QrPaymentService qrPaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testValidateFlow_shouldReturnValid() throws Exception {
        // Create transaction
        var tx = qrTransactionService.createTransaction("mb", "123456789", "10000", "Test payment",
                "integration-test", 300L, qrCodeService.getSigningKeyId());
        assertNotNull(tx);

        // Build QR content
        String qr = qrCodeService.buildQRContent(tx.getBankCode(), tx.getBankAccount(), tx.getAmount(),
                tx.getMessage(), tx.getTransactionId(), tx.getCreatedAt().getEpochSecond(),
                tx.getExpireAt().getEpochSecond(), tx.getSignatureKeyId());
        assertNotNull(qr);

        var parsed = qrCodeService.parseTlv(qr);
        assertTrue(parsed.containsKey("62"), "Parsed tlv should contain 62");

        var sub62 = qrCodeService.parseTlv(parsed.get("62"));
        assertTrue(sub62.containsKey("05"), "62 should contain transaction id 05");

        // Call validate endpoint
        QrValidateRequest validateReq = new QrValidateRequest();
        validateReq.setQrContent(qr);
        String requestJson = objectMapper.writeValueAsString(validateReq);

        String responseBody = mockMvc.perform(post("/api/qr/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        assertNotNull(apiResponse);

        QrValidateResponse validateResp = objectMapper.convertValue(apiResponse.getData(), QrValidateResponse.class);
        assertTrue(validateResp.isValid(), "QR should be valid");
        assertEquals(tx.getTransactionId(), validateResp.getTransactionId());
    }

    @Test
    void testValidateFlow_truncatedMessage_shouldStillReturnValid() throws Exception {
        // Create transaction with a very long message
        String longMsg = IntStream.range(0, 120)
                .mapToObj(i -> "x")
                .collect(Collectors.joining());

        var tx = qrTransactionService.createTransaction("mb", "123456789", "10000", longMsg,
                "integration-test", 300L, qrCodeService.getSigningKeyId());
        assertNotNull(tx);

        String qr = qrCodeService.buildQRContent(tx.getBankCode(), tx.getBankAccount(), tx.getAmount(),
                tx.getMessage(), tx.getTransactionId(), tx.getCreatedAt().getEpochSecond(),
                tx.getExpireAt().getEpochSecond(), tx.getSignatureKeyId());
        assertNotNull(qr);

        var parsed = qrCodeService.parseTlv(qr);
        assertTrue(parsed.containsKey("62"));

        var sub62 = qrCodeService.parseTlv(parsed.get("62"));
        assertTrue(sub62.containsKey("05"));

        QrValidateRequest validateReq = new QrValidateRequest();
        validateReq.setQrContent(qr);
        String requestJson = objectMapper.writeValueAsString(validateReq);

        String responseBody = mockMvc.perform(post("/api/qr/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        assertNotNull(apiResponse);

        QrValidateResponse validateResp = objectMapper.convertValue(apiResponse.getData(), QrValidateResponse.class);
        assertTrue(validateResp.isValid(), "QR should be valid even with truncated 62");
        assertEquals(tx.getTransactionId(), validateResp.getTransactionId());
    }

    @Test
    void testValidateFlow_missing62FallbackByBankAccountAndAmount() throws Exception {
        // Create transaction
        var tx = qrTransactionService.createTransaction("mb", "123456789", "10000", "No62 message",
                "integration-test", 300L, qrCodeService.getSigningKeyId());
        assertNotNull(tx);

        // Build a QR string without 62 (minimal, simulating stripped data)
        String qrWithout62 = "00020101021238520010A000000727012200069704220108123456780208QRIBFTTA530370454061000005802VN5905Ezami6002HN63040000";

        QrValidateRequest validateReq = new QrValidateRequest();
        validateReq.setQrContent(qrWithout62);
        String requestJson = objectMapper.writeValueAsString(validateReq);

        String responseBody = mockMvc.perform(post("/api/qr/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        assertNotNull(apiResponse);

        QrValidateResponse validateResp = objectMapper.convertValue(apiResponse.getData(), QrValidateResponse.class);
        assertNotNull(validateResp.getMessage());
    }

    @Test
    void testGetTransactionEndpointReturnsFullMessage() throws Exception {
        String longMsg = IntStream.range(0, 200)
                .mapToObj(i -> "m")
                .collect(Collectors.joining());

        var tx = qrTransactionService.createTransaction("mb", "123456789", "10000", longMsg,
                "integration-test", 300L, qrCodeService.getSigningKeyId());
        assertNotNull(tx);

        String responseBody = mockMvc.perform(get("/api/qr/transaction/" + tx.getTransactionId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        assertNotNull(apiResponse);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        assertEquals(longMsg, data.get("message"));
    }

    @Test
    void testDebugParse_truncatedTlv_shouldReturnDiagnostics() {
        String truncated = "000201010212386300";
        QrDebugRequest req = new QrDebugRequest();
        req.setQrContent(truncated);

        // Call controller method directly
        var qrValidationService = new QRCodeValidationService(qrCodeService, qrTransactionService, qrPaymentGrantService);
        var controller = new QRCodeController(qrCodeService, qrTransactionService, qrPaymentService,
                simpleRateLimiter, qrPaymentGrantService, qrValidationService);

        QrDebugResponse data = controller.debugParse(req);
        assertNotNull(data);
        assertFalse(data.getDiagnostics().isEmpty(), "Diagnostics should not be empty for malformed TLV");

        boolean found = data.getDiagnostics().stream()
                .anyMatch(d -> d.toLowerCase().contains("malformed length") || d.toLowerCase().contains("malformed"));
        assertTrue(found, "Diagnostics should mention malformed length information");
    }
}
