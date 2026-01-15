package com.hth.udecareer.controllers;

import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.service.SePayWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"SEPAY_WEBHOOK_API_KEY=abcd1234"})
class SePayWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SePayWebhookService sePayWebhookService;

    @Test
    void webhookRequiresApiKeyWhenConfigured() throws Exception {
        String payload = "{\"id\": 1, \"transferAmount\": 1000}";

        // Mock the service behavior
        when(sePayWebhookService.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        when(sePayWebhookService.process(any())).thenReturn(ApiResponse.success("ok"));

        String signature = "ignored-sign";

        // Without API key -> HTTP 401
        mockMvc.perform(post("/hooks/sepay-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-SePay-Signature", signature))
                .andExpect(status().isUnauthorized());

        // With wrong API key -> HTTP 401
        mockMvc.perform(post("/hooks/sepay-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-SePay-Signature", signature)
                        .header("Authorization", "Apikey wrongkey"))
                .andExpect(status().isUnauthorized());

        // With correct API key -> HTTP 200
        mockMvc.perform(post("/hooks/sepay-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-SePay-Signature", signature)
                        .header("Authorization", "Apikey abcd1234"))
                .andExpect(status().isOk());
    }
}
