package com.hth.udecareer.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RevenueCatWebhookDto {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookEvent {
        @JsonProperty("api_version")
        private String apiVersion;
        
        @JsonProperty("event")
        private Event event;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        // Loại event: INITIAL_PURCHASE, RENEWAL, CANCELLATION, EXPIRATION, etc.
        @JsonProperty("type")
        private String type;

        // ID của event
        @JsonProperty("id")
        private String id;

        // Thời gian event xảy ra
        @JsonProperty("event_timestamp_ms")
        private Long eventTimestampMs;

        // App user ID (MD5 của email)
        @JsonProperty("app_user_id")
        private String appUserId;

        // Product ID
        @JsonProperty("product_id")
        private String productId;

        // Entitlement IDs
        @JsonProperty("entitlement_ids")
        private String[] entitlementIds;

        // Subscription info
        @JsonProperty("subscriber_attributes")
        private SubscriberAttributes subscriberAttributes;

        // Expiration date
        @JsonProperty("expiration_at_ms")
        private Long expirationAtMs;

        // Purchase date
        @JsonProperty("purchased_at_ms")
        private Long purchasedAtMs;

        // Store (PLAY_STORE, APP_STORE)
        @JsonProperty("store")
        private String store;

        // Environment (PRODUCTION, SANDBOX)
        @JsonProperty("environment")
        private String environment;

        // Price
        @JsonProperty("price")
        private Double price;

        @JsonProperty("store_transaction_id")
        private String storeTransactionId;

        @JsonProperty("presented_offering_id")
        private String presentedOfferingId;

        // Currency
        @JsonProperty("currency")
        private String currency;

        // Period type
        @JsonProperty("period_type")
        private String periodType; // TRIAL, INTRO, NORMAL
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriberAttributes {
        @JsonProperty("$email")
        private EmailAttribute email;

        @JsonProperty("$displayName")
        private EmailAttribute displayName;

        @JsonProperty("$phoneNumber")
        private EmailAttribute phoneNumber;
    }

    @Data
    public static class EmailAttribute {
        @JsonProperty("value")
        private String value;

        @JsonProperty("updated_at_ms")
        private Long updatedAtMs;
    }
}