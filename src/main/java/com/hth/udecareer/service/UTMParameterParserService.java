package com.hth.udecareer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@Service
public class UTMParameterParserService {

    /**
     * Parse UTM parameters from a URL
     * Returns a map with keys: campaign, medium, source
     */
    public UTMParameters parseUTMParameters(String url) {
        if (url == null || url.trim().isEmpty()) {
            return new UTMParameters(null, null, null);
        }

        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            
            if (query == null || query.isEmpty()) {
                return new UTMParameters(null, null, null);
            }

            UriComponents components = UriComponentsBuilder.fromUriString(url).build();
            Map<String, String> queryParams = components.getQueryParams().toSingleValueMap();

            String campaign = queryParams.get("utm_campaign");
            String medium = queryParams.get("utm_medium");
            String source = queryParams.get("utm_source");

            // If no UTM parameters, try to extract source from referrer
            if (source == null || source.isEmpty()) {
                source = extractSourceFromUrl(uri);
            }

            return new UTMParameters(
                campaign != null && !campaign.isEmpty() ? campaign : null,
                medium != null && !medium.isEmpty() ? medium : null,
                source != null && !source.isEmpty() ? source : null
            );
        } catch (Exception e) {
            log.warn("Error parsing UTM parameters from URL: {}", url, e);
            return new UTMParameters(null, null, null);
        }
    }

    /**
     * Extract source domain from URL
     */
    private String extractSourceFromUrl(URI uri) {
        if (uri.getHost() != null) {
            String host = uri.getHost();
            // Remove www. prefix
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        }
        return null;
    }

    /**
     * Data class for UTM parameters
     */
    public static class UTMParameters {
        private final String campaign;
        private final String medium;
        private final String source;

        public UTMParameters(String campaign, String medium, String source) {
            this.campaign = campaign;
            this.medium = medium;
            this.source = source;
        }

        public String getCampaign() {
            return campaign;
        }

        public String getMedium() {
            return medium;
        }

        public String getSource() {
            return source;
        }
    }
}

