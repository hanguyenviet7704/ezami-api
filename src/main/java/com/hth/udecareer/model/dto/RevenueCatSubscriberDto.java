package com.hth.udecareer.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RevenueCatSubscriberDto {

    private Subscriber subscriber;

    @Data
    public static class Subscriber {
        private Map<String, Entitlement> entitlements;
    }

    @Data
    public static class Entitlement {
        @JsonProperty("expires_date")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime expiresDate;

        @JsonProperty("grace_period_expires_date")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime gracePeriodExpiresDate;

        @JsonProperty("purchase_date")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime purchaseDate;

        @JsonProperty("product_identifier")
        private String productIdentifier;
    }
}
