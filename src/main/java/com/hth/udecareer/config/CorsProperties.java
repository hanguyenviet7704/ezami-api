package com.hth.udecareer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "cors")
@Data
@Validated
public class CorsProperties {

    @NotEmpty(message = "CORS allowed origins must not be empty")
    private List<@NotNull String> allowedOrigins;
    
    // Custom setter to handle comma-separated string from environment variable and normalize origins
    public void setAllowedOrigins(List<String> origins) {
        if (origins == null || origins.isEmpty()) {
            this.allowedOrigins = origins;
            return;
        }
        
        this.allowedOrigins = new java.util.ArrayList<>();
        
        for (String origin : origins) {
            if (origin == null || origin.trim().isEmpty()) {
                continue;
            }
            
            // Handle comma-separated string
            if (origin.contains(",")) {
                String[] parts = origin.split(",");
                for (String part : parts) {
                    String normalized = normalizeOrigin(part);
                    if (!normalized.isEmpty()) {
                        this.allowedOrigins.add(normalized);
                    }
                }
            } else {
                // Single origin - normalize it
                String normalized = normalizeOrigin(origin);
                if (!normalized.isEmpty()) {
                    this.allowedOrigins.add(normalized);
                }
            }
        }
    }
    
    // Normalize origin: trim, remove path and trailing slash
    // CORS origin should only contain: protocol://host:port (no path)
    private String normalizeOrigin(String origin) {
        if (origin == null) {
            return "";
        }
        String normalized = origin.trim();
        
        // Remove any path after domain (CORS origin should not have path)
        // Example: https://example.com/path -> https://example.com
        try {
            java.net.URL url = new java.net.URL(normalized);
            // Rebuild origin with only protocol, host, and port (no path)
            StringBuilder sb = new StringBuilder();
            sb.append(url.getProtocol()).append("://").append(url.getHost());
            if (url.getPort() != -1) {
                sb.append(":").append(url.getPort());
            }
            normalized = sb.toString();
        } catch (java.net.MalformedURLException e) {
            // If URL parsing fails, try simple string manipulation
            // Remove everything after the first slash that's not part of protocol
            int protocolEnd = normalized.indexOf("://");
            if (protocolEnd != -1) {
                int pathStart = normalized.indexOf("/", protocolEnd + 3);
                if (pathStart != -1) {
                    normalized = normalized.substring(0, pathStart);
                }
            }
            // Remove trailing slash if still present
            if (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
        }
        
        return normalized;
    }

    @NotEmpty(message = "CORS allowed methods must not be empty")
    private List<@NotNull String> allowedMethods;

    @NotEmpty(message = "CORS allowed headers must not be empty")
    private List<@NotNull String> allowedHeaders;

    @NotEmpty(message = "CORS exposed headers must not be empty")
    private List<@NotNull String> exposedHeaders;

    @NotNull(message = "CORS allowCredentials must not be null")
    private boolean allowCredentials = false;

    @Positive(message = "CORS maxAge must be a positive number")
    private long maxAge = 3600;

    private boolean allowPrivateNetwork = false;

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^https?://[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?))*$");
    private static final Pattern LOCALHOST_PATTERN = Pattern.compile("^https?://(localhost|127\\.0\\.0\\.1|0\\.0\\.0\\.0)(:[0-9]+)?$");

    public boolean isValid() {
        try {
            validateConfiguration();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void validateConfiguration() {
        validateAllowedOrigins();
        validateAllowedMethods();
        validateAllowedHeaders();
        validateExposedHeaders();
        validateMaxAge();
        validateCredentialsSecurity();
    }

    private void validateAllowedOrigins() {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException("Origin list must not be null or empty");
        }

        for (int i = 0; i < allowedOrigins.size(); i++) {
            String origin = allowedOrigins.get(i);
            if (origin == null || origin.trim().isEmpty()) {
                throw new IllegalArgumentException("Origin must not be null or empty");
            }

            // Normalize origin: trim and remove trailing slash
            origin = origin.trim();
            if (origin.endsWith("/")) {
                origin = origin.substring(0, origin.length() - 1);
                // Update the list with normalized origin
                allowedOrigins.set(i, origin);
            }

            if ("*".equals(origin)) {
                if (allowCredentials) {
                    throw new IllegalArgumentException("Wildcard (*) cannot be used when allowCredentials = true");
                }
                continue;
            }

            if ("null".equals(origin)) {
                throw new IllegalArgumentException("'null' origin is not allowed for security reasons");
            }

            if (!isValidDomain(origin)) {
                throw new IllegalArgumentException("Invalid origin: " + origin);
            }
        }
    }

    private void validateAllowedMethods() {
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            throw new IllegalArgumentException("Allowed methods must not be empty");
        }

        String[] validMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"};

        for (String method : allowedMethods) {
            if (method == null || method.trim().isEmpty()) {
                throw new IllegalArgumentException("Method must not be null or empty");
            }

            boolean isValid = false;
            for (String validMethod : validMethods) {
                if (validMethod.equals(method.toUpperCase())) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                throw new IllegalArgumentException("Invalid method: " + method);
            }
        }
    }

    private void validateAllowedHeaders() {
        if (allowedHeaders == null || allowedHeaders.isEmpty()) {
            throw new IllegalArgumentException("Allowed headers must not be empty");
        }

        for (String header : allowedHeaders) {
            if (header == null || header.trim().isEmpty()) {
                throw new IllegalArgumentException("Header must not be null or empty");
            }

            if ("*".equals(header) && allowCredentials) {
                throw new IllegalArgumentException("Wildcard (*) cannot be used for headers when allowCredentials = true");
            }
        }
    }

    private void validateExposedHeaders() {
        if (exposedHeaders == null || exposedHeaders.isEmpty()) {
            throw new IllegalArgumentException("Exposed headers must not be empty");
        }

        for (String header : exposedHeaders) {
            if (header == null || header.trim().isEmpty()) {
                throw new IllegalArgumentException("Exposed header must not be null or empty");
            }
        }
    }

    private void validateMaxAge() {
        if (maxAge <= 0) {
            throw new IllegalArgumentException("Max age must be a positive number");
        }

        if (maxAge > 86400) { // 24 hours
            throw new IllegalArgumentException("Max age must not exceed 24 hours (86400 seconds)");
        }
    }

    private void validateCredentialsSecurity() {
        if (allowCredentials) {
            if (allowedOrigins != null && allowedOrigins.contains("*")) {
                throw new IllegalArgumentException("Wildcard (*) cannot be used for origins when allowCredentials = true");
            }

            if (allowedHeaders != null && allowedHeaders.contains("*")) {
                throw new IllegalArgumentException("Wildcard (*) cannot be used for headers when allowCredentials = true");
            }
        }
    }

    private boolean isValidDomain(String domain) {
        return DOMAIN_PATTERN.matcher(domain).matches() || LOCALHOST_PATTERN.matcher(domain).matches();
    }

    public String[] getAllowedOriginsArray() {
        return allowedOrigins != null ? allowedOrigins.toArray(new String[0]) : new String[0];
    }

    public String[] getAllowedMethodsArray() {
        return allowedMethods != null ? allowedMethods.toArray(new String[0]) : new String[0];
    }

    public String[] getAllowedHeadersArray() {
        return allowedHeaders != null ? allowedHeaders.toArray(new String[0]) : new String[0];
    }

    public String[] getExposedHeadersArray() {
        return exposedHeaders != null ? exposedHeaders.toArray(new String[0]) : new String[0];
    }

    public boolean isOriginAllowed(String origin) {
        if (origin == null || allowedOrigins == null) {
            return false;
        }
        return allowedOrigins.contains(origin) || allowedOrigins.contains("*");
    }

    public String getConfigurationInfo() {
        return String.format(
                "CORS Config - Origins: %d, Methods: %d, Headers: %d, Credentials: %s, MaxAge: %d",
                allowedOrigins != null ? allowedOrigins.size() : 0,
                allowedMethods != null ? allowedMethods.size() : 0,
                allowedHeaders != null ? allowedHeaders.size() : 0,
                allowCredentials,
                maxAge
        );
    }
}