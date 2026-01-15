package com.hth.udecareer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dynamic CORS Filter hỗ trợ pattern matching cho origins
 * Đặc biệt hỗ trợ tất cả subdomain của .vercel.app
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DynamicCorsFilter extends OncePerRequestFilter {

    private final CorsProperties corsProperties;

    // Pattern để match tất cả subdomain Vercel (bao gồm cả http và https)
    private static final Pattern VERCEL_PATTERN = Pattern.compile(
        "^https?://[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.vercel\\.app$"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        
        if (origin != null && !origin.isEmpty()) {
            // Kiểm tra xem origin có được phép không
            if (isOriginAllowed(origin)) {
                // Set CORS headers
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", 
                    String.join(", ", corsProperties.getAllowedMethods()));
                response.setHeader("Access-Control-Allow-Headers", 
                    String.join(", ", corsProperties.getAllowedHeaders()));
                response.setHeader("Access-Control-Expose-Headers", 
                    String.join(", ", corsProperties.getExposedHeaders()));
                response.setHeader("Access-Control-Max-Age", String.valueOf(corsProperties.getMaxAge()));
                
                log.debug("CORS headers added for origin: {}", origin);
            } else {
                log.warn("Origin not allowed: {}", origin);
            }
        }

        // Handle preflight request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Kiểm tra xem origin có được phép không
     * - Cho phép các origin trong danh sách configured
     * - Cho phép TẤT CẢ subdomain của .vercel.app (http và https)
     */
    private boolean isOriginAllowed(String origin) {
        if (origin == null || origin.isEmpty()) {
            return false;
        }

        // Normalize origin - remove trailing slash
        String normalizedOrigin = origin.trim();
        if (normalizedOrigin.endsWith("/")) {
            normalizedOrigin = normalizedOrigin.substring(0, normalizedOrigin.length() - 1);
        }

        // 1. Kiểm tra trong danh sách configured origins
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (allowedOrigins.contains("*")) {
            log.debug("Wildcard origin allowed");
            return true;
        }

        // Check exact match
        for (String allowedOrigin : allowedOrigins) {
            if (normalizedOrigin.equalsIgnoreCase(allowedOrigin)) {
                log.debug("Origin matched in configured list: {}", normalizedOrigin);
                return true;
            }
        }

        // 2. Kiểm tra xem có phải Vercel domain không
        if (VERCEL_PATTERN.matcher(normalizedOrigin).matches()) {
            log.info("Vercel domain allowed: {}", normalizedOrigin);
            return true;
        }

        return false;
    }
}
