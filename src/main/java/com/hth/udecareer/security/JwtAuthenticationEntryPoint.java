package com.hth.udecareer.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CorsConfigurationSource corsConfigurationSource;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        log.warn("Unauthorized access attempt: {}", authException.getMessage());
        
        // Đảm bảo CORS headers được thêm vào error response
        String origin = request.getHeader("Origin");
        if (origin != null && corsConfigurationSource != null) {
            CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(request);
            if (corsConfig != null && corsConfig.checkOrigin(origin) != null) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin");
            }
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Trả về JSON error response
        String jsonResponse = "{\"error\":\"Unauthorized\",\"message\":\"Invalid credentials\"}";
        response.getWriter().write(jsonResponse);
    }
}
