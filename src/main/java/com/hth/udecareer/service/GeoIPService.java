package com.hth.udecareer.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service để lookup country và city từ IP address
 * Sử dụng ip-api.com (free, không cần API key cho basic usage)
 * 
 * Rate limit: 45 requests/minute (đủ cho development)
 * Production nên dùng MaxMind GeoIP2 database hoặc paid API
 * 
 * API endpoint: http://ip-api.com/json/{ip}?fields=status,message,countryCode,city
 * Response format: {"status":"success","countryCode":"VN","city":"Ho Chi Minh City"}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIPService {

    private static final String IP_API_URL = "http://ip-api.com/json/{ip}?fields=status,message,countryCode,city";

    private final RestTemplate restTemplate;

    /**
     * Get country code và city từ IP address
     * 
     * @param ipAddress IP address string
     * @return GeoIPResult với country code và city, hoặc null nếu không lookup được
     */
    public GeoIPResult getLocationFromIP(String ipAddress) {
        // Validate IP address
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            log.debug("IP address is null or empty, skipping GeoIP lookup");
            return null;
        }

        try {
            // Call ip-api.com API để lookup country và city
            ResponseEntity<IPApiResponse> response = restTemplate.getForEntity(
                    IP_API_URL, 
                    IPApiResponse.class, 
                    ipAddress.trim()
            );

            IPApiResponse apiResponse = response.getBody();
            if (apiResponse != null && "success".equals(apiResponse.getStatus())) {
                log.debug("GeoIP lookup successful for IP {}: country={}, city={}", 
                        ipAddress, apiResponse.getCountryCode(), apiResponse.getCity());
                return new GeoIPResult(
                        apiResponse.getCountryCode(),
                        apiResponse.getCity()
                );
            } else {
                log.warn("GeoIP lookup failed for IP {}: {}", ipAddress, 
                        apiResponse != null ? apiResponse.getMessage() : "Unknown error");
            }
        } catch (Exception e) {
            // Log error nhưng không throw - GeoIP failure không nên block tracking
            // Tracking vẫn tiếp tục dù GeoIP lookup fail
            log.warn("Error looking up GeoIP for IP {}: {}", ipAddress, e.getMessage());
        }

        return null;
    }

    /**
     * Result class cho GeoIP lookup
     */
    @Data
    public static class GeoIPResult {
        private final String countryCode;  // 2-letter country code: VN, US, etc.
        private final String city;          // City name

        public GeoIPResult(String countryCode, String city) {
            this.countryCode = countryCode;
            this.city = city;
        }
    }

    /**
     * Response model từ ip-api.com
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class IPApiResponse {
        private String status;  // "success" or "fail"
        
        @JsonProperty("message")
        private String message;  // Error message nếu fail
        
        @JsonProperty("countryCode")
        private String countryCode;  // 2-letter country code
        
        @JsonProperty("city")
        private String city;  // City name
    }
}

