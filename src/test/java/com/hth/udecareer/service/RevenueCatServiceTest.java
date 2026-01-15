package com.hth.udecareer.service;

import com.hth.udecareer.model.dto.RevenueCatSubscriberDto;
import com.hth.udecareer.setting.RevenueCatSetting;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RevenueCatService using MockWebServer.
 */
class RevenueCatServiceTest {

    private MockWebServer mockWebServer;
    private RevenueCatService revenueCatService;
    private RevenueCatSetting revenueCatSetting;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Configure settings to point to mock server
        revenueCatSetting = new RevenueCatSetting();
        revenueCatSetting.setApiBaseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""));
        revenueCatSetting.setSecretApiKey(TEST_API_KEY);

        RevenueCatSetting.Endpoint endpoint = new RevenueCatSetting.Endpoint();
        endpoint.setSubscribers("subscribers");
        revenueCatSetting.setEndpoints(endpoint);

        revenueCatService = new RevenueCatService(revenueCatSetting);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getSubscriber_withValidResponse_shouldReturnSubscriber() throws Exception {
        // given
        String responseBody = """
            {
                "subscriber": {
                    "entitlements": {
                        "premium": {
                            "expires_date": "2024-12-31T23:59:59Z",
                            "purchase_date": "2024-01-01T00:00:00Z",
                            "product_identifier": "premium_monthly"
                        }
                    }
                }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // when
        Optional<RevenueCatSubscriberDto> result = revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getSubscriber());
        assertNotNull(result.get().getSubscriber().getEntitlements());
        assertTrue(result.get().getSubscriber().getEntitlements().containsKey("premium"));

        RevenueCatSubscriberDto.Entitlement entitlement =
                result.get().getSubscriber().getEntitlements().get("premium");
        assertEquals("premium_monthly", entitlement.getProductIdentifier());
    }

    @Test
    void getSubscriber_withNotFoundResponse_shouldReturnEmpty() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"Subscriber not found\"}"));

        // when
        Optional<RevenueCatSubscriberDto> result = revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void getSubscriber_withServerError_shouldReturnEmpty() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal server error\"}"));

        // when
        Optional<RevenueCatSubscriberDto> result = revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void getSubscriber_shouldSendCorrectAuthorizationHeader() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"subscriber\": {\"entitlements\": {}}}"));

        // when
        revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("Bearer " + TEST_API_KEY, request.getHeader("Authorization"));
        assertEquals("application/json", request.getHeader("accept"));
    }

    @Test
    void getSubscriber_shouldCallCorrectEndpoint() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"subscriber\": {\"entitlements\": {}}}"));

        // when
        revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/subscribers/" + TEST_USER_ID, request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void getSubscriber_withEmptyEntitlements_shouldReturnSubscriber() throws Exception {
        // given
        String responseBody = """
            {
                "subscriber": {
                    "entitlements": {}
                }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200));

        // when
        Optional<RevenueCatSubscriberDto> result = revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getSubscriber().getEntitlements());
        assertTrue(result.get().getSubscriber().getEntitlements().isEmpty());
    }

    @Test
    void getSubscriber_withMultipleEntitlements_shouldParseAll() throws Exception {
        // given
        String responseBody = """
            {
                "subscriber": {
                    "entitlements": {
                        "premium": {
                            "product_identifier": "premium_monthly"
                        },
                        "pro": {
                            "product_identifier": "pro_yearly"
                        }
                    }
                }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setResponseCode(200));

        // when
        Optional<RevenueCatSubscriberDto> result = revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getSubscriber().getEntitlements().size());
        assertTrue(result.get().getSubscriber().getEntitlements().containsKey("premium"));
        assertTrue(result.get().getSubscriber().getEntitlements().containsKey("pro"));
    }

    @Test
    void getSubscriber_withUnauthorized_shouldReturnEmpty() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"Unauthorized\"}"));

        // when
        Optional<RevenueCatSubscriberDto> result = revenueCatService.getSubscriber(TEST_USER_ID);

        // then
        assertFalse(result.isPresent());
    }
}
