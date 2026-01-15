package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.request.PatternAnalysisRequest;
import com.hth.udecareer.eil.model.response.PatternAnalysisResponse;
import com.hth.udecareer.eil.service.PatternDetectionService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/patterns")
@Tag(name = "EIL Pattern Analysis", description = "Learning pattern detection and analysis APIs")
public class PatternAnalysisController {

    private final PatternDetectionService patternDetectionService;
    private final UserRepository userRepository;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze session patterns", description = "Analyze a completed session to detect learning patterns")
    public ResponseEntity<ApiResponse> analyzeSession(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody PatternAnalysisRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Analyzing patterns for user {} session {}", userId, request.getSessionId());

        PatternAnalysisResponse response = patternDetectionService.analyzeSession(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get session analysis", description = "Get pattern analysis for a specific session")
    public ResponseEntity<ApiResponse> getSessionAnalysis(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting pattern analysis for session {}", sessionId);

        PatternAnalysisResponse response = patternDetectionService.getBySessionId(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get pattern history", description = "Get pattern analysis history for user")
    public ResponseEntity<ApiResponse> getHistory(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Category code filter") @RequestParam(required = false) String categoryCode,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting pattern history for user {}", userId);

        Page<PatternAnalysisResponse> response = patternDetectionService.getHistory(userId, categoryCode, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get pattern stats", description = "Get aggregated pattern statistics")
    public ResponseEntity<ApiResponse> getStats(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Category code filter") @RequestParam(required = false) String categoryCode) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting pattern stats for user {}", userId);

        PatternAnalysisResponse.PatternStats response = patternDetectionService.getStats(userId, categoryCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Delete session analysis", description = "Delete pattern analysis for a specific session")
    public ResponseEntity<ApiResponse> deleteSessionAnalysis(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Deleting pattern analysis for session {}", sessionId);

        patternDetectionService.deleteBySessionId(userId, sessionId);
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
