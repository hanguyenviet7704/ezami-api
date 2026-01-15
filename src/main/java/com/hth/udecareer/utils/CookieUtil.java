package com.hth.udecareer.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CookieUtil {

    @Value("${jwt.cookie.name:accessToken}")
    private String cookieName;

    @Value("${jwt.cookie.max-age:604800}") // 7 days
    private int cookieMaxAge;

    @Value("${jwt.cookie.secure:true}")
    private boolean secure;

    @Value("${jwt.cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${jwt.cookie.same-site:None}")
    private String sameSite;

    /**
     * Lấy thời gian sống của cookie (max age in seconds)
     */
    public int getCookieMaxAge() {
        return cookieMaxAge;
    }

    /**
     * Tạo cookie chứa JWT token
     */
    public Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(httpOnly);  // Không cho JS access - bảo mật hơn
        cookie.setSecure(secure);      // Chỉ gửi qua HTTPS (nên true khi production)
        cookie.setPath("/");           // Cookie có hiệu lực toàn site
        cookie.setMaxAge(cookieMaxAge); // Thời gian sống
        return cookie;
    }

    /**
     * Thêm cookie vào response với SameSite attribute
     * Sử dụng ResponseCookie để đảm bảo SameSite attribute được set đúng cách
     */
    public void addJwtCookie(HttpServletResponse response, String token) {
        try {
            // Kiểm tra xem response đã bị committed chưa
            if (response.isCommitted()) {
                log.error("Response already committed! Cannot set cookie.");
                throw new IllegalStateException("Response already committed, cannot set cookie");
            }
            
            log.info("Setting JWT cookie: name={}, secure={}, httpOnly={}, sameSite={}, maxAge={}, committed={}", 
                cookieName, secure, httpOnly, sameSite, cookieMaxAge, response.isCommitted());
            
            // Sử dụng ResponseCookie của Spring Framework để set cookie đúng cách
            // ResponseCookie hỗ trợ đầy đủ attributes bao gồm SameSite
            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, token)
                    .path("/")
                    .maxAge(cookieMaxAge)
                    .httpOnly(httpOnly)
                    .secure(secure);
            
            // Set SameSite attribute (ResponseCookie.sameSite() nhận String)
            if (sameSite != null && !sameSite.isEmpty()) {
                // Validate và set SameSite value
                String sameSiteValue = sameSite.toUpperCase();
                if (sameSiteValue.equals("NONE") || sameSiteValue.equals("LAX") || sameSiteValue.equals("STRICT")) {
                    cookieBuilder.sameSite(sameSiteValue);
                } else {
                    log.warn("Invalid SameSite value: {}, using NONE for cross-origin", sameSite);
                    cookieBuilder.sameSite("None");
                }
            } else {
                // Default to None for cross-origin
                cookieBuilder.sameSite("None");
            }
            
            // Build cookie
            ResponseCookie cookie = cookieBuilder.build();
            
            // Set cookie header - ResponseCookie tự động format đúng
            response.addHeader("Set-Cookie", cookie.toString());
            
            // Verify header đã được set
            String setCookieHeader = response.getHeader("Set-Cookie");
            if (setCookieHeader != null) {
                // Extract token from Set-Cookie header for verification
                String tokenInCookie = extractTokenFromSetCookie(setCookieHeader);
                if (tokenInCookie != null && tokenInCookie.equals(token)) {
                    log.info("JWT cookie set successfully: token matches (length: {})", token.length());
                } else {
                    log.warn("JWT cookie token mismatch! Expected length: {}, Got length: {}", 
                        token.length(), tokenInCookie != null ? tokenInCookie.length() : 0);
                }
            } else {
                log.warn("Set-Cookie header not found after setting!");
            }
            
        } catch (Exception e) {
            log.error("Error setting JWT cookie: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to set JWT cookie", e);
        }
    }

    /**
     * Extract token value from Set-Cookie header string
     * Format: "accessToken=<token>; Path=/; Max-Age=..."
     */
    private String extractTokenFromSetCookie(String setCookieHeader) {
        if (setCookieHeader == null || setCookieHeader.isEmpty()) {
            return null;
        }
        // Find the token value between cookie name and first semicolon
        String cookieNamePrefix = cookieName + "=";
        int startIndex = setCookieHeader.indexOf(cookieNamePrefix);
        if (startIndex == -1) {
            return null;
        }
        startIndex += cookieNamePrefix.length();
        int endIndex = setCookieHeader.indexOf(';', startIndex);
        if (endIndex == -1) {
            // No semicolon found, token goes to end of string
            endIndex = setCookieHeader.length();
        }
        return setCookieHeader.substring(startIndex, endIndex);
    }

    /**
     * Lấy JWT token từ cookie
     */
    public String getJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Xóa JWT cookie (dùng khi logout)
     * Phải set đúng các attributes (Secure, SameSite) để match với cookie đã set
     */
    public void deleteJwtCookie(HttpServletResponse response) {
        try {
            // Kiểm tra xem response đã bị committed chưa
            if (response.isCommitted()) {
                log.error("Response already committed! Cannot delete cookie.");
                throw new IllegalStateException("Response already committed, cannot delete cookie");
            }
            
            log.info("Deleting JWT cookie: name={}, secure={}, httpOnly={}, sameSite={}", 
                cookieName, secure, httpOnly, sameSite);
            
            // Sử dụng ResponseCookie để xóa cookie đúng cách
            // QUAN TRỌNG: Phải set đúng các attributes (Secure, SameSite) để match với cookie đã set
            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, "")
                    .path("/")
                    .maxAge(0) // Xóa ngay lập tức
                    .httpOnly(httpOnly)
                    .secure(secure); // Phải set Secure giống như khi set cookie
            
            // Set SameSite attribute - PHẢI GIỐNG như khi set cookie
            if (sameSite != null && !sameSite.isEmpty()) {
                String sameSiteValue = sameSite.toUpperCase();
                if (sameSiteValue.equals("NONE") || sameSiteValue.equals("LAX") || sameSiteValue.equals("STRICT")) {
                    cookieBuilder.sameSite(sameSiteValue);
                } else {
                    log.warn("Invalid SameSite value: {}, using NONE for cross-origin", sameSite);
                    cookieBuilder.sameSite("None");
                }
            } else {
                // Default to None for cross-origin
                cookieBuilder.sameSite("None");
            }
            
            // Build cookie
            ResponseCookie cookie = cookieBuilder.build();
            
            // Set cookie header - ResponseCookie tự động format đúng
            response.addHeader("Set-Cookie", cookie.toString());
            
            // Verify header đã được set
            String setCookieHeader = response.getHeader("Set-Cookie");
            if (setCookieHeader != null) {
                log.info("JWT cookie deleted successfully");
            } else {
                log.warn("Set-Cookie header not found after deleting cookie!");
            }
            
        } catch (Exception e) {
            log.error("Error deleting JWT cookie: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete JWT cookie", e);
        }
    }



    /**
     * Lấy affiliateId từ cookie affiliate_ref (chứa trực tiếp affiliateId)
     */
    public String getAffiliateIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        for (Cookie cookie : request.getCookies()) {
            if ("affiliate_ref".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Lấy affiliate_link_id từ cookie
     */
    public String getAffiliateLinkIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        for (Cookie cookie : request.getCookies()) {
            if ("affiliate_link_id".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}