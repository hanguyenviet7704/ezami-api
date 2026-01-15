package com.hth.udecareer.service;

import com.hth.udecareer.entities.AffiliateLink;
import com.hth.udecareer.entities.AffiliateSetting;
import com.hth.udecareer.entities.AffiliateVisit;
import com.hth.udecareer.enums.DeviceType;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.ClickTrackingData;
import com.hth.udecareer.model.request.UpdateLinkRequest;
import com.hth.udecareer.model.response.AffiliateLinkResponse;
import com.hth.udecareer.model.response.AffiliateLinkStatsResponse;
import com.hth.udecareer.repository.AffiliateLinkRepository;
import com.hth.udecareer.repository.AffiliateSettingRepository;
import com.hth.udecareer.repository.AffiliateVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateLinkServiceImpl implements AffiliateLinkService {

    private static final String DEFAULT_TRACKING_PARAM = "ref";
    private static final String REFERRAL_VARIABLE_KEY = "referral_variable";
    private static final long CACHE_TTL_MS = 3600000;

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    private final AffiliateSettingRepository affiliateSettingRepository;
    private final AffiliateLinkRepository affiliateLinkRepository;
    private final AffiliateVisitRepository affiliateVisitRepository;

    @Value("${app.affiliate.base-url}")
    private String baseUrl;

    // In-memory cache fields
    private volatile String cachedTrackingParam;
    private volatile long lastCacheTime = 0;

    @Override
    public String generateAffiliateLink(Long affiliateId, String originalUrl) {
        if (affiliateId == null || originalUrl == null || originalUrl.isEmpty()) {
            log.warn("Invalid parameters for generating affiliate link: affiliateId={}, originalUrl={}", affiliateId, originalUrl);
            return originalUrl;
        }

        String trackingParamName = getTrackingParamName();

        try {
            String affiliateLink = UriComponentsBuilder.fromUriString(originalUrl)
                    .queryParam(trackingParamName, affiliateId)
                    .build()
                    .toUriString();

            log.info("Generated affiliate link for affiliate {}: {}", affiliateId, affiliateLink);
            return affiliateLink;
        } catch (Exception e) {
            log.error("Error generating affiliate link for affiliate {}: {}", affiliateId, e.getMessage(), e);
            return originalUrl;
        }
    }


    private String getTrackingParamName() {
        long currentTime = System.currentTimeMillis();

        // Check if cache is valid
        if (cachedTrackingParam != null && (currentTime - lastCacheTime) < CACHE_TTL_MS) {
            log.debug("Returning cached tracking parameter name: {}", cachedTrackingParam);
            return cachedTrackingParam;
        }

        // Cache expired or not initialized - fetch from database with synchronization
        synchronized (this) {
            // Double-check after acquiring lock (another thread might have updated it)
            currentTime = System.currentTimeMillis();
            if (cachedTrackingParam != null && (currentTime - lastCacheTime) < CACHE_TTL_MS) {
                log.debug("Returning cached tracking parameter name (after lock): {}", cachedTrackingParam);
                return cachedTrackingParam;
            }

            log.debug("Cache expired or empty, fetching tracking parameter from database");

            try {
                Optional<AffiliateSetting> setting = affiliateSettingRepository.findBySettingKey(REFERRAL_VARIABLE_KEY);

                if (setting.isPresent() && setting.get().getSettingValue() != null && !setting.get().getSettingValue().isEmpty()) {
                    String paramName = setting.get().getSettingValue();

                    // Update cache
                    cachedTrackingParam = paramName;
                    lastCacheTime = System.currentTimeMillis();

                    log.debug("Using tracking parameter name from settings (cached): {}", paramName);
                    return paramName;
                }
            } catch (Exception e) {
                log.error("Error fetching tracking parameter name from settings: {}", e.getMessage(), e);
            }

            // Use default and cache it
            cachedTrackingParam = DEFAULT_TRACKING_PARAM;
            lastCacheTime = System.currentTimeMillis();

            log.debug("Using default tracking parameter name (cached): {}", DEFAULT_TRACKING_PARAM);
            return DEFAULT_TRACKING_PARAM;
        }
    }

    @Override
    @Transactional
    public String createPrettyLink(Long affiliateId, String targetUrl, String desiredSlug) {
        log.info("Creating pretty link for affiliate {}: slug={}, targetUrl={}", affiliateId, desiredSlug, targetUrl);

        // Validation 1: Check if slug is not empty
        if (desiredSlug == null || desiredSlug.trim().isEmpty()) {
            log.error("Slug cannot be empty");
            throw new IllegalArgumentException("Slug cannot be empty");
        }

        String trimmedSlug = desiredSlug.trim();

        // Validation 2: Validate slug format using regex
        if (!SLUG_PATTERN.matcher(trimmedSlug).matches()) {
            log.error("Invalid slug format: {}. Only alphanumeric characters and dashes are allowed", trimmedSlug);
            throw new IllegalArgumentException(
                "Invalid slug format: '" + trimmedSlug + "'. Only alphanumeric characters and dashes are allowed"
            );
        }

        // Validation 3: Check uniqueness
        if (affiliateLinkRepository.existsByShortUrl(trimmedSlug)) {
            log.error("Short URL already exists: {}", trimmedSlug);
            throw new RuntimeException("Slug already exists: " + trimmedSlug);
        }

        // Validate other parameters
        if (affiliateId == null) {
            log.error("Affiliate ID cannot be null");
            throw new IllegalArgumentException("Affiliate ID cannot be null");
        }

        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            log.error("Target URL cannot be empty");
            throw new IllegalArgumentException("Target URL cannot be empty");
        }

        // Create and persist the AffiliateLink
        AffiliateLink affiliateLink = new AffiliateLink();
        affiliateLink.setAffiliateId(affiliateId);
        affiliateLink.setOriginalUrl(targetUrl.trim());

        // Generate affiliate URL with tracking parameter
        String generatedAffiliateUrl = generateAffiliateLink(affiliateId, targetUrl.trim());
        affiliateLink.setAffiliateUrl(generatedAffiliateUrl);

        // Set the short URL (slug)
        affiliateLink.setShortUrl(trimmedSlug);

        // Set default values
        affiliateLink.setIsActive(true);
        affiliateLink.setTotalClicks(0);
        affiliateLink.setUniqueClicks(0);
        affiliateLink.setTotalConversions(0);
        affiliateLink.setTotalCommission(BigDecimal.ZERO);

        try {
            String prettyUrl = baseUrl + trimmedSlug;
            affiliateLink.setPrettyUrl(prettyUrl);
            
            // Save first to get the ID
            AffiliateLink saved = affiliateLinkRepository.save(affiliateLink);
            
            // Update affiliateUrl with affiliateLinkId after we have the ID
            String finalAffiliateUrl = UriComponentsBuilder.fromUriString(generatedAffiliateUrl)
                    .queryParam("affiliateLinkId", saved.getId())
                    .build()
                    .toUriString();
            saved.setAffiliateUrl(finalAffiliateUrl);
            
            // Save again with updated affiliateUrl
            saved = affiliateLinkRepository.save(saved);

            log.info("Successfully created pretty link: {} -> {} (affiliate: {})", saved.getPrettyUrl(), targetUrl, finalAffiliateUrl);
            return saved.getPrettyUrl();
        } catch (Exception e) {
            log.error("Error saving affiliate link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create pretty link: " + e.getMessage(), e);
        }
    }

    @Override
    @Async
    @Transactional
    public void trackClick(Long linkId, Long affiliateId, String ip, String userAgent, String referer, String landingUrl) {
        try {
            log.info("Async tracking click for link ID: {}, affiliate: {}, IP: {}", linkId, affiliateId, ip);

            // Validation: Check if the link exists
            Optional<AffiliateLink> linkOptional = affiliateLinkRepository.findById(linkId);
            if (linkOptional.isEmpty()) {
                log.warn("Cannot track click: Link ID {} not found", linkId);
                return;
            }

            AffiliateLink affiliateLink = linkOptional.get();

            // Check if this is a unique click (IP + User Agent not seen in last 24h)
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            boolean isUnique = !affiliateVisitRepository.existsByLinkIdAndIpAddressAndUserAgentAndCreatedAtAfter(
                    linkId,
                    ip,
                    userAgent,
                    oneDayAgo
            );

            // Update click counters in the AffiliateLink entity
            affiliateLink.setTotalClicks(affiliateLink.getTotalClicks() + 1);

            // Only increment unique clicks if this is truly unique
            if (isUnique) {
                affiliateLink.setUniqueClicks(affiliateLink.getUniqueClicks() + 1);
            }

            affiliateLinkRepository.save(affiliateLink);
            log.debug("Updated click counters for link ID {}: total={}, unique={}, isUniqueClick={}",
                     linkId, affiliateLink.getTotalClicks(), affiliateLink.getUniqueClicks(), isUnique);

            // Create and save AffiliateVisit record
            AffiliateVisit visit = new AffiliateVisit();
            visit.setAffiliateId(affiliateId);
            visit.setLinkId(linkId);
            visit.setIpAddress(ip);
            visit.setUserAgent(userAgent);
            visit.setReferrerUrl(referer);
            visit.setLandingUrl(landingUrl);

            // Mark visit as unique based on IP + User Agent + 24h window
            visit.setIsUnique(isUnique);

            // Not converted yet
            visit.setIsConverted(false);

            affiliateVisitRepository.save(visit);
            log.info("Successfully tracked click: link_id={}, affiliate_id={}, visit_id={}",
                    linkId, affiliateId, visit.getId());

        } catch (Exception e) {
            // Error handling: Log the error but don't crash the application
            log.error("Error tracking click for link ID {}: {}", linkId, e.getMessage(), e);
            // Do not rethrow - tracking failures should not affect user experience
        }
    }

    /**
     * Track click với đầy đủ thông tin tracking
     * 
     * Method này được thực hiện ASYNC để không block redirect flow
     * Nếu có lỗi, chỉ log lại nhưng không throw exception
     * 
     * Flow:
     * 1. Validate link tồn tại
     * 2. Check click có unique không (IP + UA trong 24h)
     * 3. Update click counters trong AffiliateLink
     * 4. Save AffiliateVisit record với đầy đủ tracking data
     * 
     * @param trackingData ClickTrackingData object với đầy đủ thông tin
     */
    @Override
    @Async  // Thực hiện async để không block redirect
    @Transactional  // Đảm bảo transaction cho database operations
    public void trackClick(ClickTrackingData trackingData) {
        try {
            log.info("Async tracking click with full data for link ID: {}, affiliate: {}, IP: {}",
                    trackingData.getLinkId(), trackingData.getAffiliateId(), trackingData.getIpAddress());

            // ========== STEP 1: VALIDATE LINK ==========
            // Kiểm tra link có tồn tại trong database không
            // (Có thể bị xóa sau khi user click nhưng trước khi track)
            Optional<AffiliateLink> linkOptional = affiliateLinkRepository.findById(trackingData.getLinkId());
            if (linkOptional.isEmpty()) {
                log.warn("Cannot track click: Link ID {} not found", trackingData.getLinkId());
                return;  // Exit early nếu link không tồn tại
            }

            AffiliateLink affiliateLink = linkOptional.get();

            // ========== STEP 2: CHECK UNIQUE CLICK ==========
            // Click được coi là unique nếu:
            // - Cùng linkId
            // - Cùng IP address
            // - Cùng User Agent
            // - Trong vòng 24 giờ
            // - Chưa có record nào match trong database
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            boolean isUnique = !affiliateVisitRepository.existsByLinkIdAndIpAddressAndUserAgentAndCreatedAtAfter(
                    trackingData.getLinkId(),
                    trackingData.getIpAddress(),
                    trackingData.getUserAgent(),
                    oneDayAgo
            );

            // ========== STEP 3: UPDATE CLICK COUNTERS ==========
            // Update tổng số clicks (luôn tăng)
            affiliateLink.setTotalClicks(affiliateLink.getTotalClicks() + 1);

            // Chỉ tăng unique clicks nếu click này thực sự unique
            // Tránh double counting khi user click nhiều lần
            if (isUnique) {
                affiliateLink.setUniqueClicks(affiliateLink.getUniqueClicks() + 1);
            }

            // Save updated counters
            affiliateLinkRepository.save(affiliateLink);
            log.debug("Updated click counters for link ID {}: total={}, unique={}, isUniqueClick={}",
                     trackingData.getLinkId(), affiliateLink.getTotalClicks(), affiliateLink.getUniqueClicks(), isUnique);

            // ========== STEP 4: SAVE VISIT RECORD ==========
            // Tạo AffiliateVisit record với đầy đủ tracking data
            // Record này dùng để:
            // - Hiển thị click reports
            // - Phân tích traffic (device, browser, OS, source, etc.)
            // - Track conversions
            AffiliateVisit visit = new AffiliateVisit();
            
            // ========== BASIC INFO ==========
            visit.setAffiliateId(trackingData.getAffiliateId());  // ID của affiliate
            visit.setLinkId(trackingData.getLinkId());  // ID của affiliate link
            visit.setIpAddress(trackingData.getIpAddress());  // IP address của user
            visit.setUserAgent(trackingData.getUserAgent());  // Full user agent string
            visit.setReferrerUrl(trackingData.getReferrerUrl());  // URL trang web mà user đến từ đó
            visit.setLandingUrl(trackingData.getLandingUrl());  // Full landing URL (bao gồm query params)
            
            // ========== UTM PARAMETERS ==========
            visit.setCampaign(trackingData.getCampaign());  // utm_campaign từ URL
            visit.setMedium(trackingData.getMedium());  // utm_medium từ URL
            visit.setSource(trackingData.getSource());  // utm_source từ URL hoặc extract từ referrer
            
            // ========== DEVICE INFORMATION ==========
            // Device type: DESKTOP, MOBILE, TABLET, UNKNOWN
            visit.setDeviceType(trackingData.getDeviceType() != null ? trackingData.getDeviceType() : DeviceType.UNKNOWN);
            visit.setBrowser(trackingData.getBrowser());  // Browser name: Chrome, Firefox, Safari, Edge, etc.
            visit.setOs(trackingData.getOs());  // OS name: Windows, macOS, Linux, iOS, Android
            
            // ========== GEOGRAPHIC INFO ==========
            // Chưa implement GeoIP service, để null
            // Có thể thêm sau bằng cách tích hợp GeoIP API (MaxMind, ipapi.co, etc.)
            visit.setCountry(trackingData.getCountry());  // Country code (2 letters): VN, US, etc.
            visit.setCity(trackingData.getCity());  // City name: "Ho Chi Minh", "Hanoi", etc.
            
            // ========== SESSION & COOKIE TRACKING ==========
            visit.setSessionId(trackingData.getSessionId());  // Session ID để track user session
            visit.setCookieValue(trackingData.getCookieValue());  // Cookie value từ affiliate_ref cookie
            visit.setExpiresAt(trackingData.getExpiresAt());  // Cookie expiration date (30 days from now)
            
            // ========== CLICK METADATA ==========
            visit.setIsUnique(isUnique);  // Click có unique không (dựa trên IP + UA trong 24h)
            visit.setIsConverted(false);  // Chưa convert (sẽ update thành true khi có order)
            
            // ========== CONVERSION TRACKING ==========
            // Các fields này sẽ được update sau khi user đăng ký/mua hàng
            visit.setConvertedAt(null);  // Thời gian convert (null cho đến khi có conversion)
            visit.setReferralId(null);  // ID của referral record (null cho đến khi có conversion)

            // Save visit record
            affiliateVisitRepository.save(visit);
            log.info("Successfully tracked click with full data: link_id={}, affiliate_id={}, visit_id={}, device={}, browser={}, os={}",
                    trackingData.getLinkId(), trackingData.getAffiliateId(), visit.getId(),
                    visit.getDeviceType(), visit.getBrowser(), visit.getOs());

        } catch (Exception e) {
            // ========== ERROR HANDLING ==========
            // Log error nhưng KHÔNG throw exception
            // Lý do: Tracking failures không nên ảnh hưởng đến user experience
            // User vẫn được redirect bình thường dù tracking fail
            log.error("Error tracking click with full data for link ID {}: {}", trackingData.getLinkId(), e.getMessage(), e);
            // Do not rethrow - tracking failures should not affect user experience
        }
    }

    @Override
    public Page<AffiliateLinkResponse> getMyLinks(Long affiliateId, Pageable pageable) {
        log.info("Fetching links for affiliate ID: {}", affiliateId);

        Page<AffiliateLink> links = affiliateLinkRepository.findByAffiliateId(affiliateId, pageable);

        return links.map(this::toAffiliateLinkResponse);
    }

    @Override
    public AffiliateLinkResponse getLinkById(Long linkId, Long affiliateId) {
        log.info("Fetching link ID {} for affiliate ID: {}", linkId, affiliateId);

        AffiliateLink link = affiliateLinkRepository.findByIdAndAffiliateId(linkId, affiliateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return toAffiliateLinkResponse(link);
    }

    @Override
    @Transactional
    public AffiliateLinkResponse updateLink(Long linkId, UpdateLinkRequest request, Long affiliateId) {
        log.info("Updating link ID {} for affiliate ID: {}", linkId, affiliateId);

        AffiliateLink link = affiliateLinkRepository.findByIdAndAffiliateId(linkId, affiliateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Update fields
        if (request.getCampaign() != null) {
            link.setCampaign(request.getCampaign());
        }
        if (request.getMedium() != null) {
            link.setMedium(request.getMedium());
        }
        if (request.getSource() != null) {
            link.setSource(request.getSource());
        }

        affiliateLinkRepository.save(link);

        log.info("Successfully updated link ID: {}", linkId);
        return toAffiliateLinkResponse(link);
    }

    @Override
    @Transactional
    public void deactivateLink(Long linkId, Long affiliateId) {
        log.info("Deactivating link ID {} for affiliate ID: {}", linkId, affiliateId);

        AffiliateLink link = affiliateLinkRepository.findByIdAndAffiliateId(linkId, affiliateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        link.setIsActive(false);
        affiliateLinkRepository.save(link);

        log.info("Successfully deactivated link ID: {}", linkId);
    }

    @Override
    public AffiliateLinkStatsResponse getLinkStats(Long linkId, Long affiliateId) {
        log.info("Fetching stats for link ID {} for affiliate ID: {}", linkId, affiliateId);

        // Verify ownership
        AffiliateLink link = affiliateLinkRepository.findByIdAndAffiliateId(linkId, affiliateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Overall stats
        Long totalClicks = affiliateVisitRepository.countByLinkId(linkId);
        Long uniqueClicks = affiliateVisitRepository.countByLinkIdAndIsUnique(linkId, true);
        Long totalConversions = affiliateVisitRepository.countByLinkIdAndIsConverted(linkId, true);

        // Calculate conversion rate
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalClicks != null && totalClicks > 0) {
            conversionRate = BigDecimal.valueOf(totalConversions != null ? totalConversions : 0)
                    .divide(BigDecimal.valueOf(totalClicks), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Time-based stats
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = now.minusMonths(1);

        Long clicksToday = affiliateVisitRepository.countByLinkIdAndCreatedAtAfter(linkId, startOfToday);
        Long clicksThisWeek = affiliateVisitRepository.countByLinkIdAndCreatedAtAfter(linkId, startOfWeek);
        Long clicksThisMonth = affiliateVisitRepository.countByLinkIdAndCreatedAtAfter(linkId, startOfMonth);

        Long conversionsToday = affiliateVisitRepository.countConversionsByLinkIdAndCreatedAtAfter(linkId, startOfToday);
        Long conversionsThisWeek = affiliateVisitRepository.countConversionsByLinkIdAndCreatedAtAfter(linkId, startOfWeek);
        Long conversionsThisMonth = affiliateVisitRepository.countConversionsByLinkIdAndCreatedAtAfter(linkId, startOfMonth);

        // Top sources
        List<Object[]> sourceData = affiliateVisitRepository.findTopSourcesByLinkId(linkId);
        List<Object[]> sourceConversions = affiliateVisitRepository.countConversionsByLinkIdGroupBySource(linkId);

        // Build map of conversions by source
        Map<String, Integer> conversionsBySource = sourceConversions.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        List<AffiliateLinkStatsResponse.SourceStat> topSources = sourceData.stream()
                .limit(10)
                .map(row -> AffiliateLinkStatsResponse.SourceStat.builder()
                        .source((String) row[0])
                        .clicks(((Number) row[1]).intValue())
                        .conversions(conversionsBySource.getOrDefault((String) row[0], 0))
                        .build())
                .collect(Collectors.toList());

        // Device stats
        List<Object[]> deviceData = affiliateVisitRepository.countByLinkIdGroupByDeviceType(linkId);
        AffiliateLinkStatsResponse.DeviceStats deviceStats = buildDeviceStats(deviceData);

        // Geographic stats
        List<Object[]> geoData = affiliateVisitRepository.findTopCountriesByLinkId(linkId);
        List<Object[]> countryConversions = affiliateVisitRepository.countConversionsByLinkIdGroupByCountry(linkId);

        // Build map of conversions by country
        Map<String, Integer> conversionsByCountry = countryConversions.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        List<AffiliateLinkStatsResponse.GeoStat> topCountries = geoData.stream()
                .limit(10)
                .map(row -> AffiliateLinkStatsResponse.GeoStat.builder()
                        .country((String) row[0])
                        .clicks(((Number) row[1]).intValue())
                        .conversions(conversionsByCountry.getOrDefault((String) row[0], 0))
                        .build())
                .collect(Collectors.toList());

        return AffiliateLinkStatsResponse.builder()
                .linkId(linkId)
                .shortUrl(link.getShortUrl())
                .prettyUrl(link.getPrettyUrl())
                .originalUrl(link.getOriginalUrl())
                .totalClicks(totalClicks != null ? totalClicks.intValue() : 0)
                .uniqueClicks(uniqueClicks != null ? uniqueClicks.intValue() : 0)
                .totalConversions(totalConversions != null ? totalConversions.intValue() : 0)
                .totalCommission(link.getTotalCommission())
                .conversionRate(conversionRate)
                .clicksToday(clicksToday != null ? clicksToday.intValue() : 0)
                .clicksThisWeek(clicksThisWeek != null ? clicksThisWeek.intValue() : 0)
                .clicksThisMonth(clicksThisMonth != null ? clicksThisMonth.intValue() : 0)
                .conversionsToday(conversionsToday != null ? conversionsToday.intValue() : 0)
                .conversionsThisWeek(conversionsThisWeek != null ? conversionsThisWeek.intValue() : 0)
                .conversionsThisMonth(conversionsThisMonth != null ? conversionsThisMonth.intValue() : 0)
                .topSources(topSources)
                .deviceStats(deviceStats)
                .topCountries(topCountries)
                .build();
    }

    // Helper methods
    private AffiliateLinkResponse toAffiliateLinkResponse(AffiliateLink link) {
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (link.getTotalClicks() != null && link.getTotalClicks() > 0) {
            conversionRate = BigDecimal.valueOf(link.getTotalConversions() != null ? link.getTotalConversions() : 0)
                    .divide(BigDecimal.valueOf(link.getTotalClicks()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return AffiliateLinkResponse.builder()
                .linkId(link.getId())
                .affiliateId(link.getAffiliateId())
                .originalUrl(link.getOriginalUrl())
                .affiliateUrl(link.getAffiliateUrl())
                .shortUrl(link.getShortUrl())
                .prettyUrl(link.getPrettyUrl())
                .campaign(link.getCampaign())
                .medium(link.getMedium())
                .source(link.getSource())
                .linkType(link.getLinkType() != null ? link.getLinkType().toString() : null)
                .isActive(link.getIsActive())
                .totalClicks(link.getTotalClicks())
                .uniqueClicks(link.getUniqueClicks())
                .totalConversions(link.getTotalConversions())
                .totalCommission(link.getTotalCommission())
                .conversionRate(conversionRate)
                .createdAt(link.getCreatedAt())
                .expiresAt(link.getExpiresAt())
                .build();
    }

    private AffiliateLinkStatsResponse.DeviceStats buildDeviceStats(List<Object[]> deviceData) {
        int desktop = 0, mobile = 0, tablet = 0, unknown = 0;

        for (Object[] row : deviceData) {
            DeviceType deviceType = (DeviceType) row[0];
            int count = ((Number) row[1]).intValue();

            if (deviceType == null) {
                unknown += count;
            } else {
                switch (deviceType) {
                    case DESKTOP:
                        desktop = count;
                        break;
                    case MOBILE:
                        mobile = count;
                        break;
                    case TABLET:
                        tablet = count;
                        break;
                    default:
                        unknown += count;
                        break;
                }
            }
        }

        return AffiliateLinkStatsResponse.DeviceStats.builder()
                .desktop(desktop)
                .mobile(mobile)
                .tablet(tablet)
                .unknown(unknown)
                .build();
    }
}

