package com.hth.udecareer.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.model.dto.RevenueCatSubscriberDto;
import com.hth.udecareer.setting.RevenueCatSetting;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class RevenueCatService {

    private final RevenueCatSetting revenueCatSetting;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Autowired
    public RevenueCatService(RevenueCatSetting revenueCatSetting) {
        this(revenueCatSetting, new OkHttpClient());
    }

    // Constructor for testing with custom OkHttpClient
    RevenueCatService(RevenueCatSetting revenueCatSetting, OkHttpClient client) {
        this.revenueCatSetting = revenueCatSetting;
        this.client = client;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Optional<RevenueCatSubscriberDto> getSubscriber(final String appUserId) throws IOException {
        final Request request = new Request.Builder()
                .url("%s/%s/%s".formatted(
                        revenueCatSetting.getApiBaseUrl(),
                        revenueCatSetting.getEndpoints().getSubscribers(),
                        appUserId))
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + revenueCatSetting.getSecretApiKey())
                .build();

        try (final Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (final ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        return Optional.of(objectMapper.readValue(responseBody.string(), RevenueCatSubscriberDto.class));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
