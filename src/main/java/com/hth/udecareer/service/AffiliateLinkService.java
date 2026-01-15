package com.hth.udecareer.service;

import com.hth.udecareer.model.dto.ClickTrackingData;
import com.hth.udecareer.model.request.UpdateLinkRequest;
import com.hth.udecareer.model.response.AffiliateLinkResponse;
import com.hth.udecareer.model.response.AffiliateLinkStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AffiliateLinkService {

    String generateAffiliateLink(Long affiliateId, String originalUrl);

    String createPrettyLink(Long affiliateId, String targetUrl, String desiredSlug);

    void trackClick(Long linkId, Long affiliateId, String ip, String userAgent, String referer, String landingUrl);

    void trackClick(ClickTrackingData trackingData);

    // New CRUD methods
    Page<AffiliateLinkResponse> getMyLinks(Long affiliateId, Pageable pageable);

    AffiliateLinkResponse getLinkById(Long linkId, Long affiliateId);

    AffiliateLinkResponse updateLink(Long linkId, UpdateLinkRequest request, Long affiliateId);

    void deactivateLink(Long linkId, Long affiliateId);

    AffiliateLinkStatsResponse getLinkStats(Long linkId, Long affiliateId);
}
