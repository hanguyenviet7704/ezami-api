package com.hth.udecareer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CorsConfig {

    private final CorsProperties corsProperties;

    @PostConstruct
    public void validateCorsConfiguration() {
        try {
            log.info("Validating CORS configuration...");
            corsProperties.validateConfiguration();
            log.info("CORS configuration is valid: {}", corsProperties.getConfigurationInfo());
        } catch (IllegalArgumentException e) {
            log.error("Invalid CORS configuration: {}", e.getMessage());
            throw new IllegalStateException("Invalid CORS configuration: " + e.getMessage(), e);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Creating CORS configuration source...");
        log.info("CORS is handled by DynamicCorsFilter for pattern-based origin matching (e.g., *.vercel.app)");

        CorsConfiguration configuration = new CorsConfiguration();

        configureAllowedOrigins(configuration);
        configureAllowedMethods(configuration);
        configureAllowedHeaders(configuration);
        configureExposedHeaders(configuration);

        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        if (corsProperties.isAllowPrivateNetwork()) {
            log.warn("Private network access is enabled - only recommended for development environments");
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configuration source created successfully");
        return source;
    }

    private void configureAllowedOrigins(CorsConfiguration configuration) {
        List<String> origins = corsProperties.getAllowedOrigins();

        if (origins.contains("*")) {
            if (corsProperties.isAllowCredentials()) {
                throw new IllegalStateException(
                        "SECURITY ERROR: Wildcard (*) origins are not allowed when allowCredentials = true"
                );
            }
            log.warn("Using wildcard (*) for origins - not safe for production environments");
            configuration.addAllowedOriginPattern("*");
        } else {
            // When allowCredentials is true, use setAllowedOrigins for exact match
            // This ensures better browser compatibility
            if (corsProperties.isAllowCredentials()) {
                configuration.setAllowedOrigins(origins);
                log.info("{} allowed origins configured (exact match for credentials)", origins.size());
            } else {
                configuration.setAllowedOriginPatterns(origins);
                log.info("{} allowed origins configured (pattern match)", origins.size());
            }
        }
    }

    private void configureAllowedMethods(CorsConfiguration configuration) {
        List<String> methods = corsProperties.getAllowedMethods();
        configuration.setAllowedMethods(methods);
        log.info("{} allowed methods configured: {}", methods.size(), methods);
    }

    private void configureAllowedHeaders(CorsConfiguration configuration) {
        List<String> headers = corsProperties.getAllowedHeaders();

        if (headers.contains("*")) {
            if (corsProperties.isAllowCredentials()) {
                throw new IllegalStateException(
                        "SECURITY ERROR: Wildcard (*) headers are not allowed when allowCredentials = true"
                );
            }
            log.warn("Using wildcard (*) for headers - not safe for production environments");
            configuration.addAllowedHeader("*");
        } else {
            configuration.setAllowedHeaders(headers);
            log.info("{} allowed headers configured", headers.size());
        }
    }

    private void configureExposedHeaders(CorsConfiguration configuration) {
        List<String> exposedHeaders = corsProperties.getExposedHeaders();
        configuration.setExposedHeaders(exposedHeaders);
        log.info("{} exposed headers configured", exposedHeaders.size());
    }

    @Bean("developmentCorsConfiguration")
    public CorsConfiguration developmentCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://0.0.0.0:*"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        log.info("Development CORS configuration created");
        return config;
    }

    @Bean("productionCorsConfiguration")
    public CorsConfiguration productionCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
                "https://yourdomain.com",
                "https://www.yourdomain.com",
                "https://app.yourdomain.com"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
        ));
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(1800L);
        log.info("Production CORS configuration created");
        return config;
    }

    private boolean isProduction() {
        String profile = System.getProperty("spring.profiles.active");
        return "release".equals(profile) || "production".equals(profile);
    }

    public CorsConfiguration getEnvironmentSpecificConfiguration() {
        if (isProduction()) {
            return productionCorsConfiguration();
        } else {
            return developmentCorsConfiguration();
        }
    }
}