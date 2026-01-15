package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.request.TimeEstimateRequest;
import com.hth.udecareer.eil.model.response.TimeEstimateResponse;
import com.hth.udecareer.eil.service.TimeEstimationService;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/estimates")
@Tag(name = "EIL Time Estimation", description = "Time-to-Certification estimation APIs")
public class TimeEstimationController {

    private final TimeEstimationService timeEstimationService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create estimate", description = "Create a new time estimate for a certification")
    public ResponseEntity<ApiResponse> createEstimate(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody TimeEstimateRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Creating time estimate for user {} cert {}", userId, request.getCertificationCode());

        TimeEstimateResponse response = timeEstimationService.createEstimate(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{certificationCode}")
    @Operation(summary = "Get estimate", description = "Get time estimate for a specific certification")
    public ResponseEntity<ApiResponse> getEstimate(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Certification code") @PathVariable String certificationCode) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting time estimate for user {} cert {}", userId, certificationCode);

        TimeEstimateResponse response = timeEstimationService.getEstimate(userId, certificationCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all estimates", description = "Get all time estimates for user")
    public ResponseEntity<ApiResponse> getAllEstimates(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting all time estimates for user {}", userId);

        List<TimeEstimateResponse> response = timeEstimationService.getAllEstimates(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active estimates", description = "Get active time estimates for user")
    public ResponseEntity<ApiResponse> getActiveEstimates(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting active time estimates for user {}", userId);

        List<TimeEstimateResponse> response = timeEstimationService.getActiveEstimates(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{certificationCode}/progress")
    @Operation(summary = "Update progress", description = "Update progress on a time estimate")
    public ResponseEntity<ApiResponse> updateProgress(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Certification code") @PathVariable String certificationCode,
            @Valid @RequestBody TimeEstimateRequest.ProgressUpdateRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Updating progress for user {} cert {}", userId, certificationCode);

        TimeEstimateResponse response = timeEstimationService.updateProgress(userId, certificationCode, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{certificationCode}/status")
    @Operation(summary = "Update status", description = "Update status of a time estimate")
    public ResponseEntity<ApiResponse> updateStatus(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Certification code") @PathVariable String certificationCode,
            @Parameter(description = "New status") @RequestParam String status) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Updating status to {} for user {} cert {}", status, userId, certificationCode);

        TimeEstimateResponse response = timeEstimationService.updateStatus(userId, certificationCode, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{certificationCode}/pace")
    @Operation(summary = "Get pace analysis", description = "Get pace analysis for a certification goal")
    public ResponseEntity<ApiResponse> getPaceAnalysis(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Certification code") @PathVariable String certificationCode) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting pace analysis for user {} cert {}", userId, certificationCode);

        TimeEstimateResponse.PaceAnalysis response = timeEstimationService.getPaceAnalysis(userId, certificationCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{certificationCode}")
    @Operation(summary = "Delete estimate", description = "Delete a time estimate")
    public ResponseEntity<ApiResponse> deleteEstimate(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Certification code") @PathVariable String certificationCode) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Deleting time estimate for user {} cert {}", userId, certificationCode);

        timeEstimationService.deleteEstimate(userId, certificationCode);
        return ResponseEntity.ok(ApiResponse.success());
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
