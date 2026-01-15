package com.hth.udecareer.service;

import com.hth.udecareer.config.TimezoneConfig;
import com.hth.udecareer.entities.ReportEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.XProfileEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ReportRequest;
import com.hth.udecareer.model.request.ReportUpdateRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.ReportResponse;
import com.hth.udecareer.model.response.ReportStatsResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.repository.ReportRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.XProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ReportRepository reportRepository;
    private final XProfileRepository xProfileRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    private static final List<String> VALID_OBJECT_TYPES = Arrays.asList(
            ReportEntity.OBJECT_TYPE_FEED,
            ReportEntity.OBJECT_TYPE_COMMENT,
            ReportEntity.OBJECT_TYPE_USER,
            ReportEntity.OBJECT_TYPE_SPACE
    );

    private static final List<String> VALID_REASONS = Arrays.asList(
            ReportEntity.REASON_SPAM,
            ReportEntity.REASON_HARASSMENT,
            ReportEntity.REASON_HATE_SPEECH,
            ReportEntity.REASON_VIOLENCE,
            ReportEntity.REASON_INAPPROPRIATE,
            ReportEntity.REASON_OTHER
    );

    private static final List<String> VALID_STATUSES = Arrays.asList(
            ReportEntity.STATUS_PENDING,
            ReportEntity.STATUS_REVIEWING,
            ReportEntity.STATUS_RESOLVED,
            ReportEntity.STATUS_DISMISSED
    );

    private static final List<String> VALID_ACTIONS = Arrays.asList(
            ReportEntity.ACTION_NONE,
            ReportEntity.ACTION_CONTENT_REMOVED,
            ReportEntity.ACTION_USER_WARNED,
            ReportEntity.ACTION_USER_BANNED
    );

    /**
     * Submit a new report
     */
    @Transactional
    public ReportResponse submitReport(Long reporterId, ReportRequest request) {
        // Validate object type
        if (!VALID_OBJECT_TYPES.contains(request.getObjectType())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid object type. Must be one of: " + String.join(", ", VALID_OBJECT_TYPES));
        }

        // Validate reason
        if (!VALID_REASONS.contains(request.getReason())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid reason. Must be one of: " + String.join(", ", VALID_REASONS));
        }

        // Check for duplicate report
        Optional<ReportEntity> existingReport = reportRepository.findExistingReport(
                reporterId, request.getObjectId(), request.getObjectType());
        if (existingReport.isPresent()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "You have already reported this content");
        }

        // Determine reported user ID based on object type
        Long reportedUserId = null;
        if (ReportEntity.OBJECT_TYPE_USER.equals(request.getObjectType())) {
            reportedUserId = request.getObjectId();
        }
        // For feed/comment, we could look up the user who created it
        // But we'll keep it simple for now

        // Create report
        ReportEntity report = ReportEntity.builder()
                .reporterId(reporterId)
                .reportedUserId(reportedUserId)
                .objectId(request.getObjectId())
                .objectType(request.getObjectType())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportEntity.STATUS_PENDING)
                .build();

        reportRepository.save(report);
        log.info("User {} submitted report for {} {}", reporterId, request.getObjectType(), request.getObjectId());

        return mapToResponse(report);
    }

    /**
     * Get reports for moderators with filters
     */
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> getReports(String status, String objectType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Validate status if provided
        if (status != null && !VALID_STATUSES.contains(status)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }

        // Validate object type if provided
        if (objectType != null && !VALID_OBJECT_TYPES.contains(objectType)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid object type. Must be one of: " + String.join(", ", VALID_OBJECT_TYPES));
        }

        Page<ReportEntity> reports = reportRepository.findReportsWithFilters(status, objectType, pageable);

        List<ReportResponse> items = reports.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReportResponse>builder()
                .content(items)
                .page(reports.getNumber())
                .size(reports.getSize())
                .totalElements(reports.getTotalElements())
                .totalPages(reports.getTotalPages())
                .hasNext(reports.hasNext())
                .hasPrevious(reports.hasPrevious())
                .first(reports.isFirst())
                .last(reports.isLast())
                .build();
    }

    /**
     * Get a single report by ID
     */
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        return mapToResponse(report);
    }

    /**
     * Update report status (for moderators)
     */
    @Transactional
    public ReportResponse updateReport(Long reportId, Long moderatorId, ReportUpdateRequest request) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));

        // Validate status
        if (!VALID_STATUSES.contains(request.getStatus())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }

        // Validate action if provided
        if (request.getActionTaken() != null && !VALID_ACTIONS.contains(request.getActionTaken())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid action. Must be one of: " + String.join(", ", VALID_ACTIONS));
        }

        // Update report
        report.setStatus(request.getStatus());
        report.setModeratorId(moderatorId);

        if (request.getActionTaken() != null) {
            report.setActionTaken(request.getActionTaken());
        }

        if (request.getModeratorNotes() != null) {
            report.setModeratorNotes(request.getModeratorNotes());
        }

        // Set resolved time if status is resolved or dismissed
        if (ReportEntity.STATUS_RESOLVED.equals(request.getStatus()) ||
                ReportEntity.STATUS_DISMISSED.equals(request.getStatus())) {
            report.setResolvedAt(TimezoneConfig.getCurrentVietnamTime());
        }

        reportRepository.save(report);
        log.info("Moderator {} updated report {} to status {}", moderatorId, reportId, request.getStatus());

        return mapToResponse(report);
    }

    /**
     * Delete a report
     */
    @Transactional
    public void deleteReport(Long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));

        reportRepository.delete(report);
        log.info("Report {} deleted", reportId);
    }

    /**
     * Get report statistics
     */
    @Transactional(readOnly = true)
    public ReportStatsResponse getReportStats() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportEntity.STATUS_PENDING);
        long reviewing = reportRepository.countByStatus(ReportEntity.STATUS_REVIEWING);
        long resolved = reportRepository.countByStatus(ReportEntity.STATUS_RESOLVED);
        long dismissed = reportRepository.countByStatus(ReportEntity.STATUS_DISMISSED);

        return ReportStatsResponse.builder()
                .totalReports(total)
                .pendingReports(pending)
                .reviewingReports(reviewing)
                .resolvedReports(resolved)
                .dismissedReports(dismissed)
                .build();
    }

    /**
     * Get user's submitted reports
     */
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> getUserReports(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportEntity> reports = reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable);

        List<ReportResponse> items = reports.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReportResponse>builder()
                .content(items)
                .page(reports.getNumber())
                .size(reports.getSize())
                .totalElements(reports.getTotalElements())
                .totalPages(reports.getTotalPages())
                .hasNext(reports.hasNext())
                .hasPrevious(reports.hasPrevious())
                .first(reports.isFirst())
                .last(reports.isLast())
                .build();
    }

    // ============= HELPER METHODS =============

    private ReportResponse mapToResponse(ReportEntity report) {
        long reportCount = reportRepository.countByObjectIdAndObjectType(report.getObjectId(), report.getObjectType());

        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporter(getXProfile(report.getReporterId()))
                .reportedUserId(report.getReportedUserId())
                .reportedUser(report.getReportedUserId() != null ? getXProfile(report.getReportedUserId()) : null)
                .objectId(report.getObjectId())
                .objectType(report.getObjectType())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .moderatorId(report.getModeratorId())
                .moderator(report.getModeratorId() != null ? getXProfile(report.getModeratorId()) : null)
                .moderatorNotes(report.getModeratorNotes())
                .actionTaken(report.getActionTaken())
                .reportCount(reportCount)
                .resolvedAt(report.getResolvedAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private XProfileResponse getXProfile(Long userId) {
        Optional<XProfileEntity> xProfileOpt = xProfileRepository.findByUserId(userId);
        Optional<User> userOpt = userRepository.findById(userId);

        String finalAvatarFromMeta = userMetaRepository.findByUserIdAndMetaKey(userId, "wpcf-avatar")
                .map(meta -> meta.getMetaValue())
                .orElse(null);

        if (xProfileOpt.isPresent()) {
            XProfileEntity xp = xProfileOpt.get();
            String finalAvatar = StringUtils.isNotBlank(xp.getAvatar()) ? xp.getAvatar() : finalAvatarFromMeta;
            String fullName = userOpt.map(User::getDisplayName).orElse(null);

            return XProfileResponse.builder()
                    .userId(xp.getUserId())
                    .username(xp.getUsername())
                    .displayName(xp.getDisplayName())
                    .fullName(fullName)
                    .avatar(finalAvatar)
                    .totalPoints(xp.getTotalPoints())
                    .isVerified(xp.getIsVerified() != null && xp.getIsVerified() == 1)
                    .build();
        } else {
            return userOpt.map(user -> XProfileResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .fullName(user.getDisplayName())
                    .avatar(finalAvatarFromMeta)
                    .totalPoints(0)
                    .isVerified(false)
                    .build())
                    .orElse(XProfileResponse.builder()
                            .userId(userId)
                            .username("")
                            .displayName("")
                            .fullName("")
                            .avatar(null)
                            .totalPoints(0)
                            .isVerified(false)
                            .build());
        }
    }
}
