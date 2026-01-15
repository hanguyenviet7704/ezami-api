package com.hth.udecareer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.service.Impl.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.apache.commons.lang3.StringUtils;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.*;

import com.hth.udecareer.model.dto.google.GoogleTokenResponse;
import com.hth.udecareer.model.dto.google.GoogleUserInfo;
import com.hth.udecareer.config.GoogleOAuthConfig;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final GoogleOAuthConfig googleOAuthConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final UserService userService;

    @Override
    public String generateGoogleAuthUrl(String state) {

        return UriComponentsBuilder.fromHttpUrl(GOOGLE_AUTH_URL)
                .queryParam("client_id", googleOAuthConfig.getClientId())
                .queryParam("redirect_uri", googleOAuthConfig.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .queryParam("state", state)
                .build()
                .toUriString();
    }


    @Override
    public User processGoogleCallback(String code) throws Exception {
        return processGoogleCallback(code, null);
    }

    @Override
    public User processGoogleCallback(String code, Long affiliateId) throws Exception {
        GoogleTokenResponse tokenResponse = getGoogleTokens(code);

        GoogleUserInfo userInfo = getGoogleUserInfo(tokenResponse.getAccessToken());

        if (userInfo.getEmail() == null) {
            log.error("Google không trả về email.");
            throw new AppException(ErrorCode.GOOGLE_AUTH_EMAIL_MISSING);
        }
        return userService.findOrCreateGoogleUser(userInfo, affiliateId);
    }

    private GoogleTokenResponse getGoogleTokens(String code) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8.toString());
        log.info("Decoded auth code: {}", decodedCode);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", decodedCode);
        body.add("client_id", googleOAuthConfig.getClientId());
        body.add("client_secret", googleOAuthConfig.getClientSecret());
        body.add("redirect_uri", googleOAuthConfig.getRedirectUri());

        body.add("grant_type", "authorization_code");

        log.info("Sending request to Google Token endpoint with redirect_uri: {}", googleOAuthConfig.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), GoogleTokenResponse.class);
            } else {
                log.error("Lỗi khi gọi Google Token API: {}", response.getBody());
                throw new AppException(ErrorCode.GOOGLE_TOKEN_EXCHANGE_FAILED);
            }
        } catch (Exception e) {
            log.error("Không thể lấy token từ Google: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.GOOGLE_TOKEN_EXCHANGE_FAILED, e);
        }
    }

    private GoogleUserInfo getGoogleUserInfo(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_URL, HttpMethod.GET, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), GoogleUserInfo.class);
            } else {
                log.error("Lỗi khi gọi Google UserInfo API: {}", response.getBody());
                throw new AppException(ErrorCode.GOOGLE_USERINFO_FETCH_FAILED);
            }
        } catch (Exception e) {
            log.error("Không thể lấy thông tin người dùng từ Google: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.GOOGLE_USERINFO_FETCH_FAILED, e);
        }
    }
}