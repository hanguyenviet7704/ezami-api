package com.hth.udecareer.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.entities.SupportLog;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.SupportChannel;
import com.hth.udecareer.enums.SupportStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.mapper.SupportMapper;
import com.hth.udecareer.model.dto.DeviceInfo;
import com.hth.udecareer.model.dto.SenderInfo;
import com.hth.udecareer.model.request.SupportLogRequest;
import com.hth.udecareer.model.request.SupportProcessRequest;
import com.hth.udecareer.model.request.SupportRequestDto;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.SupportAdminResponse;
import com.hth.udecareer.model.response.SupportLogResponse;
import com.hth.udecareer.model.response.SupportResponse;
import com.hth.udecareer.repository.SupportLogRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.SupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportServiceImpl implements SupportService {

    private final SupportLogRepository supportLogRepository;
    private final UserRepository userRepository;
    private final SupportMapper supportMapper;
    private final ObjectMapper objectMapper;
    private final UserMetaRepository userMetaRepository;

    @Override
    public LinkedHashMap<String, String> getSupportChannels() {

        LinkedHashMap<String, String> supportChannels = new LinkedHashMap<>();

        for (SupportChannel sc : SupportChannel.values()) {
            supportChannels.put(sc.getChannel(), sc.getContact());
        }

        return supportChannels;
    }

    @Override
    public SupportLogResponse saveLog(SupportLogRequest request, Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        SupportLog supportLog = supportMapper.toSupportLog(request);
        supportLog.setUserId(user.getId());

        supportLogRepository.save(supportLog);

        return SupportLogResponse.builder().build();

    }

    @Override
    public void createSupportTicket(String email, SupportRequestDto request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        SupportLog supportLog = new SupportLog();
        supportLog.setUserId(user.getId());
        supportLog.setTitle(request.getTitle().trim());
        supportLog.setDescription(request.getDescription().trim());
        supportLog.setChannel("in_app");
        supportLog.setStatus(SupportStatus.PENDING.getValue());

        try {
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                supportLog.setImageUrls(objectMapper.writeValueAsString(request.getImageUrls()));
            } else {
                supportLog.setImageUrls("[]");
            }

            if (request.getDeviceInfo() != null) {
                supportLog.setDeviceInfo(objectMapper.writeValueAsString(request.getDeviceInfo()));
            } else {
                supportLog.setDeviceInfo("{}");
            }

        } catch (Exception e) {
            log.warn("Failed to serialize support ticket data for user {}: {}", user.getId(), e.getMessage());
            supportLog.setImageUrls("[]");
            supportLog.setDeviceInfo("{}");
        }

        supportLogRepository.save(supportLog);
    }

    @Override
    public PageResponse<SupportResponse> getSupportHistory(String email, int page, int size) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        boolean isAdmin = userMetaRepository.countAdminRole(user.getId()) > 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<SupportLog> entities;
        if (isAdmin) {
            entities = supportLogRepository.findAll(pageable);
        } else {
            entities = supportLogRepository.findByUserId(user.getId(), pageable);
        }

        Page<SupportResponse> pageResult = entities.map(log -> {
            if (isAdmin) {
                return mapToAdminResponse(log);
            }else{
                return mapToUserResponse(log);
            }
        });

        return PageResponse.of(pageResult);
    }

    @Override
    public SupportResponse getSupportDetail(Long supportId) {
        SupportLog supportLog = supportLogRepository.findById(supportId).
                orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        SupportResponse supportResponse = supportMapper.toSupportResponse(supportLog);
        supportResponse.setImageUrls(parseImages(supportLog.getImageUrls()));

        return supportResponse;
    }

    @Override
    public SupportResponse processSupport(SupportProcessRequest request) {
       SupportLog supportLog = supportLogRepository.findById(request.getSupportId()).
               orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

       if (supportLog.getStatus().equals(SupportStatus.RESOLVED.getValue())) {
           throw new AppException(ErrorCode.INVALID_KEY);
       }

       if (request.getReplyMessage() == null || request.getReplyMessage().trim().isEmpty()) {
           throw new AppException(ErrorCode.VALIDATION_ERROR);
       }

       supportLog.setAdminNote(request.getReplyMessage());
       supportLog.setStatus(SupportStatus.RESOLVED.getValue());

       SupportResponse supportResponse = supportMapper.toSupportResponse(supportLogRepository.save(supportLog));
        supportResponse.setImageUrls(parseImages(supportLog.getImageUrls()));

        return supportResponse;
    }

    private SupportResponse mapToUserResponse(SupportLog log) {
        return SupportResponse.builder()
                .id(log.getId())
                .title(log.getTitle())
                .description(log.getDescription())
                .status(log.getStatus())
                .adminNote(log.getAdminNote())
                .imageUrls(parseImages(log.getImageUrls()))
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }

    private SupportAdminResponse mapToAdminResponse(SupportLog supportLog) {
        User user = userRepository.findById(supportLog.getUserId()).orElse(null);
        SenderInfo senderInfo = null;
        if (user != null) {
            senderInfo = SenderInfo.builder()
                    .id(user.getId())
                    .fullName(user.getDisplayName())
                    .email(user.getEmail())
                    .avatar(user.getUserUrl())
                    .build();
        }

        DeviceInfo deviceInfo = null;
        try {
            if (supportLog.getDeviceInfo() != null && !supportLog.getDeviceInfo().equals("{}")) {
                deviceInfo = objectMapper.readValue(supportLog.getDeviceInfo(), DeviceInfo.class);
            }
        } catch (Exception e) {
            // Log parsing failure but continue - deviceInfo is optional
            log.warn("Failed to parse deviceInfo for support log {}: {}", supportLog.getId(), e.getMessage());
        }

        return SupportAdminResponse.builder()
                .id(supportLog.getId())
                .title(supportLog.getTitle())
                .description(supportLog.getDescription())
                .status(supportLog.getStatus())
                .adminNote(supportLog.getAdminNote())
                .imageUrls(parseImages(supportLog.getImageUrls()))
                .createdAt(supportLog.getCreatedAt())
                .updatedAt(supportLog.getUpdatedAt())
                .sender(senderInfo)
                .deviceInfo(deviceInfo)
                .build();
    }

    private List<String> parseImages(String json) {
        try {
            if (json != null && !json.equals("null") && !json.equals("[]")) {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            log.debug("Failed to parse image URLs JSON: {}", e.getMessage());
        }
        return new ArrayList<>();
    }
}
