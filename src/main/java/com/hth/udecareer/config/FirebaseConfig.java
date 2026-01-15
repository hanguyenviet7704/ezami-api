package com.hth.udecareer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.config-base64:}")
    private String firebaseConfigBase64;

    @Bean
    @ConditionalOnProperty(name = "app.firebase.config-base64", matchIfMissing = false)
    public FirebaseApp firebaseApp() throws IOException {
        log.info("Initializing Firebase Admin SDK...");

        if (!StringUtils.hasText(firebaseConfigBase64)) {
            log.warn("Firebase config not provided. Firebase features will be disabled.");
            return null;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase App already initialized");
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount = null;

        try {
            log.info("Loading Firebase config from Base64 Environment Variable...");
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
            String configJson = new String(decodedBytes);

            // Check if config is empty JSON (test environment placeholder)
            if ("{}".equals(configJson.trim()) || configJson.trim().isEmpty()) {
                log.warn("Firebase config is empty or placeholder. Firebase features will be disabled.");
                return null;
            }

            serviceAccount = new ByteArrayInputStream(decodedBytes);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);

            log.info("Firebase Admin SDK initialized successfully");
            return app;

        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Firebase Base64 config: {}", e.getMessage());
            throw new IOException("Invalid Base64 Firebase Config", e);
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            throw e;
        } finally {
            if (serviceAccount != null) {
                try {
                    serviceAccount.close();
                } catch (IOException e) {
                    log.warn("Failed to close InputStream", e);
                }
            }
        }
    }
}