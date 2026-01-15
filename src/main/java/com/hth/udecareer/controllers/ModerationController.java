package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ReportRequest;
import com.hth.udecareer.model.request.ReportUpdateRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.ReportResponse;
import com.hth.udecareer.model.response.ReportStatsResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Moderation", description = "Report and Moderation APIs")
public class ModerationController {

    private final ModerationService moderationService;
    private final UserRepository userRepository;

    // ============= USER ENDPOINTS =============

    @Operation(
            summary = "Submit a report",
            description = "Submit a report for a post, comment, user, or space.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report submitted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicate report"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/moderation/report")
    public ResponseEntity<ReportResponse> submitReport(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody ReportRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        ReportResponse response = moderationService.submitReport(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get my reports",
            description = "Get list of reports submitted by the current user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reports"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/moderation/my-reports")
    public ResponseEntity<PageResponse<ReportResponse>> getMyReports(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        PageResponse<ReportResponse> response = moderationService.getUserReports(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // ============= MODERATOR/ADMIN ENDPOINTS =============

    @Operation(
            summary = "Get all reports (Moderator)",
            description = "Get paginated list of all reports with optional filters.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reports"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/moderation/reports")
    public ResponseEntity<PageResponse<ReportResponse>> getReports(
            @Parameter(description = "Filter by status: pending, reviewing, resolved, dismissed")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Filter by object type: feed, comment, user, space")
            @RequestParam(value = "object_type", required = false) String objectType,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {

        PageResponse<ReportResponse> response = moderationService.getReports(status, objectType, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get report by ID (Moderator)",
            description = "Get a specific report by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved report",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/moderation/reports/{id}")
    public ResponseEntity<ReportResponse> getReportById(
            @Parameter(description = "Report ID", example = "1")
            @PathVariable("id") Long reportId) {

        ReportResponse response = moderationService.getReportById(reportId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update report (Moderator)",
            description = "Update report status and add moderator notes.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/moderation/reports/{id}")
    public ResponseEntity<ReportResponse> updateReport(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Report ID", example = "1")
            @PathVariable("id") Long reportId,
            @Valid @RequestBody ReportUpdateRequest request) throws AppException {

        Long moderatorId = getUserIdFromPrincipal(principal);
        ReportResponse response = moderationService.updateReport(reportId, moderatorId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete report (Moderator)",
            description = "Delete a report by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @DeleteMapping("/moderation/reports/{id}")
    public ResponseEntity<Map<String, Object>> deleteReport(
            @Parameter(description = "Report ID", example = "1")
            @PathVariable("id") Long reportId) {

        moderationService.deleteReport(reportId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Report deleted successfully"
        ));
    }

    @Operation(
            summary = "Get report statistics (Moderator)",
            description = "Get statistics about reports (counts by status).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/moderation/stats")
    public ResponseEntity<ReportStatsResponse> getReportStats() {
        ReportStatsResponse response = moderationService.getReportStats();
        return ResponseEntity.ok(response);
    }

    // ============= HELPER METHODS =============

    private Long getUserIdFromPrincipal(Principal principal) throws AppException {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        return user.getId();
    }
}
