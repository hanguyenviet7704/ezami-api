package com.hth.udecareer.service;

import com.hth.udecareer.entities.AffiliateLink;
import com.hth.udecareer.model.dto.ClickTrackingData;
import com.hth.udecareer.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateTrackingService {

    // Cookie names
    private static final String AFFILIATE_SESSION_COOKIE = "affiliate_session";
    private static final String AFFILIATE_REF_COOKIE = "affiliate_ref";
    private static final String AFFILIATE_LINK_ID_COOKIE = "affiliate_link_id";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

    private final CookieUtil cookieUtil;
    private final UserAgentParserService userAgentParserService;
    private final UTMParameterParserService utmParameterParserService;
    private final GeoIPService geoIPService;


    public ClickTrackingData prepareTrackingData(
            AffiliateLink affiliateLink,
            HttpServletRequest request,
            HttpServletResponse response,
            String userAgent,
            String referer,
            String landingUrl) {

        setAffiliateCookies(affiliateLink, request, response);


        String ipAddress = extractIpAddress(request);
        

        GeoIPService.GeoIPResult geoResult = geoIPService.getLocationFromIP(ipAddress);
        String country = geoResult != null ? geoResult.getCountryCode() : null;
        String city = geoResult != null ? geoResult.getCity() : null;


        UTMParameterParserService.UTMParameters utmParams = utmParameterParserService.parseUTMParameters(landingUrl);
        String source = utmParams.getSource();
        

        if (source == null && referer != null) {
            source = extractSourceFromReferrer(referer);
        }


        com.hth.udecareer.enums.DeviceType deviceType = userAgentParserService.detectDeviceType(userAgent);
        String browser = userAgentParserService.detectBrowser(userAgent);
        String os = userAgentParserService.detectOS(userAgent);


        String sessionId = getOrCreateSessionId(request, response);


        String cookieValue = cookieUtil.getAffiliateIdFromCookie(request);
        if (cookieValue == null || cookieValue.isEmpty()) {
            cookieValue = String.valueOf(affiliateLink.getAffiliateId());
        }


        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);


        return ClickTrackingData.builder()
                .linkId(affiliateLink.getId())
                .affiliateId(affiliateLink.getAffiliateId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referrerUrl(referer)
                .landingUrl(landingUrl)
                .campaign(utmParams.getCampaign())
                .medium(utmParams.getMedium())
                .source(source)
                .deviceType(deviceType)
                .browser(browser)
                .os(os)
                .country(country)
                .city(city)
                .sessionId(sessionId)
                .cookieValue(cookieValue)
                .expiresAt(expiresAt)
                .build();
    }


    private void setAffiliateCookies(AffiliateLink affiliateLink, HttpServletRequest request, HttpServletResponse response) {

        String affiliateRefValue = String.valueOf(affiliateLink.getAffiliateId());
        ResponseCookie affiliateRefCookie = ResponseCookie.from(AFFILIATE_REF_COOKIE, affiliateRefValue)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)  // Ngăn JavaScript access
                .secure(request.isSecure())  // Chỉ gửi qua HTTPS nếu request secure
                .sameSite("Lax")  // Ngăn CSRF
                .build();
        response.addHeader("Set-Cookie", affiliateRefCookie.toString());
        log.debug("Set affiliate_ref cookie: {}", affiliateRefValue);


        String affiliateLinkIdValue = String.valueOf(affiliateLink.getId());
        ResponseCookie affiliateLinkIdCookie = ResponseCookie.from(AFFILIATE_LINK_ID_COOKIE, affiliateLinkIdValue)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", affiliateLinkIdCookie.toString());
        log.debug("Set affiliate_link_id cookie: {}", affiliateLinkIdValue);
    }


    private String getOrCreateSessionId(HttpServletRequest request, HttpServletResponse response) {

        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if (AFFILIATE_SESSION_COOKIE.equals(cookie.getName())) {
                    String sessionId = cookie.getValue();
                    if (sessionId != null && !sessionId.isEmpty()) {

                        return sessionId;
                    }
                }
            }
        }


        String sessionId = UUID.randomUUID().toString();
        

        ResponseCookie sessionCookie = ResponseCookie.from(AFFILIATE_SESSION_COOKIE, sessionId)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .build();
        
        response.addHeader("Set-Cookie", sessionCookie.toString());
        log.debug("Created new session ID: {}", sessionId);

        return sessionId;
    }


    private String extractIpAddress(HttpServletRequest request) {

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }


        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    private String extractSourceFromReferrer(String referrer) {
        if (referrer == null || referrer.trim().isEmpty()) {
            return null;
        }

        try {
            java.net.URI uri = new java.net.URI(referrer);
            String host = uri.getHost();
            if (host != null) {

                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                return host;
            }
        } catch (Exception e) {
            log.debug("Error extracting source from referrer: {}", referrer, e);
        }

        return null;
    }
}

