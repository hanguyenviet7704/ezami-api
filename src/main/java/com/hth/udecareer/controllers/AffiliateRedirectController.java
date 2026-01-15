package com.hth.udecareer.controllers;

import com.hth.udecareer.entities.AffiliateLink;
import com.hth.udecareer.model.dto.ClickTrackingData;
import com.hth.udecareer.repository.AffiliateLinkRepository;
import com.hth.udecareer.service.AffiliateLinkService;
import com.hth.udecareer.service.AffiliateTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestController
@RequestMapping("/go")
@RequiredArgsConstructor
public class AffiliateRedirectController {

    private final AffiliateLinkRepository affiliateLinkRepository;
    private final AffiliateLinkService affiliateLinkService;
    private final AffiliateTrackingService affiliateTrackingService;

    private boolean isMobileDevice(String userAgent) {
        if (userAgent == null) return false;
        String ua = userAgent.toLowerCase();
        return ua.contains("mobile") || ua.contains("android") || ua.contains("iphone") || ua.contains("ipad");
    }

    private boolean isAndroid(String userAgent) {
        if (userAgent == null) return false;
        return userAgent.toLowerCase().contains("android");
    }

    private Map<String, String> parseQueryParams(String url) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            return builder.build().getQueryParams().toSingleValueMap();
        } catch (Exception e) {
            log.warn("Failed to parse query params from URL: {}", url, e);
            return Map.of();
        }
    }


    @GetMapping("/{slug}")
    public Object redirectToAffiliateUrl(
            @PathVariable String slug,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "Unknown") String userAgent,
            @RequestHeader(value = "Referer", required = false) String referer,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            Optional<AffiliateLink> linkOptional = affiliateLinkRepository.findByShortUrl(slug);

            if (linkOptional.isEmpty()) {
                log.warn("Affiliate link not found for slug: {}", slug);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Link not found");
            }

            AffiliateLink affiliateLink = linkOptional.get();

            if (!affiliateLink.getIsActive()) {
                log.warn("Affiliate link is inactive for slug: {}", slug);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Link is no longer active");
            }

            String landingUrl = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                landingUrl += "?" + request.getQueryString();
            }

            ClickTrackingData trackingData = affiliateTrackingService.prepareTrackingData(
                    affiliateLink,
                    request,
                    response,
                    userAgent,
                    referer,
                    landingUrl
            );

            try {
                affiliateLinkService.trackClick(trackingData);
            } catch (Exception e) {
                log.error("Error tracking click for link ID {}: {}", affiliateLink.getId(), e.getMessage(), e);
            }

            if (isMobileDevice(userAgent)) {
                String affiliateUrl = affiliateLink.getAffiliateUrl();
                if (affiliateUrl == null) {
                    log.error("Affiliate URL is null for link ID {}", affiliateLink.getId());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Internal error");
                }

                Map<String, String> params = parseQueryParams(affiliateUrl);
                String quizId = params.getOrDefault("quiz", slug);
                String refCode = params.getOrDefault("ref", affiliateLink.getCampaign() != null ? affiliateLink.getCampaign() : "default");

                boolean isAndroid = isAndroid(userAgent);
                String storeLink = isAndroid ?
                    "https://play.google.com/store/apps/details?id=com.hth.udecareer&hl=vi" :
                    "https://apps.apple.com/vn/app/ezami-istqb-iiba-psm-more/id1671931331";

                String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Đang mở Ezami...</title>\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <p style=\"text-align:center; margin-top: 50px;\">Đang mở ứng dụng Ezami...</p>\n" +
                    "    <script>\n" +
                    "        var appLink = \"com.hth.udecareer://certificates/" + quizId + "?ref=" + refCode + "&affiliateLinkId=" + affiliateLink.getId() + "\";\n" +
                    "        var storeLink = \"" + storeLink + "\";\n" +
                    "        \n" +
                    "        window.location.href = appLink;\n" +
                    "        \n" +
                    "        setTimeout(function() {\n" +
                    "            window.location.href = storeLink;\n" +
                    "        }, 2500);\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(html);
            } else {
                String affiliateUrl = affiliateLink.getAffiliateUrl();
                if (affiliateUrl == null) {
                    log.error("Affiliate URL is null for link ID {}", affiliateLink.getId());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Internal error");
                }
                RedirectView redirectView = new RedirectView();
                redirectView.setUrl(affiliateUrl);
                redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);

                return redirectView;
            }
        } catch (Exception e) {
            log.error("Unexpected error in redirectToAffiliateUrl for slug {}: {}", slug, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
}
