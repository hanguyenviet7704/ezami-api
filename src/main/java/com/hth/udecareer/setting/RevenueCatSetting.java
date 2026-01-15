package com.hth.udecareer.setting;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "revenue-cat")
public class RevenueCatSetting {

    private String apiBaseUrl;

    private String secretApiKey;

    private Endpoint endpoints;

    @Data
    public static class Endpoint {
        private String subscribers;
    }
}
