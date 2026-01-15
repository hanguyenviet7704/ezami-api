package com.hth.udecareer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VnpayConfigProperties {
    private String tmnCode;
    private String hashSecret;
    private String apiUrl;
    private String returnUrl;
    private String ipnUrl;
    private String version;
    private String currency;
    private String locale;
}
