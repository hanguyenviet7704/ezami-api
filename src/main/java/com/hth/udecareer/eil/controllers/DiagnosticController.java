package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.request.DiagnosticAnswerRequest;
import com.hth.udecareer.eil.model.request.DiagnosticStartRequest;
import com.hth.udecareer.eil.model.response.DiagnosticAnswerResponse;
import com.hth.udecareer.eil.model.response.DiagnosticHistoryResponse;
import com.hth.udecareer.eil.model.response.DiagnosticResultResponse;
import com.hth.udecareer.eil.model.response.DiagnosticSessionResponse;
import com.hth.udecareer.eil.service.DiagnosticService;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/diagnostic")
@Tag(name = "EIL Diagnostic", description = "Diagnostic test APIs for initial skill assessment")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    @Operation(summary = "Start diagnostic test", description = "Start a new diagnostic test to assess initial skill levels")
    public ResponseEntity<DiagnosticSessionResponse> startDiagnostic(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody DiagnosticStartRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Starting diagnostic test for user {}", userId);
        DiagnosticSessionResponse response = diagnosticService.startDiagnostic(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/next-question/{sessionId}")
    @Operation(summary = "Get next question", description = "Get the next adaptive question for diagnostic session")
    public ResponseEntity<DiagnosticAnswerResponse> getNextQuestion(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Diagnostic session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting next diagnostic question for user {}, session {}", userId, sessionId);
        DiagnosticAnswerResponse response = diagnosticService.getNextQuestion(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/answer")
    @Operation(summary = "Submit answer", description = "Submit an answer for a diagnostic question. Returns next question or auto-finishes if termination conditions met.")
    public ResponseEntity<DiagnosticAnswerResponse> submitAnswer(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody DiagnosticAnswerRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Submitting diagnostic answer for user {}, session {}", userId, request.getSessionId());
        DiagnosticAnswerResponse response = diagnosticService.submitAnswer(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish/{sessionId}")
    @Operation(summary = "Finish diagnostic test", description = "Finish diagnostic test and get results")
    public ResponseEntity<DiagnosticResultResponse> finishDiagnostic(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Diagnostic session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Finishing diagnostic test {} for user {}", sessionId, userId);
        DiagnosticResultResponse response = diagnosticService.finishDiagnostic(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active diagnostic session", description = "Get the active (IN_PROGRESS) diagnostic session for current user. Returns null data if no active session exists.")
    public ResponseEntity<DiagnosticSessionResponse> getActiveSession(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting active diagnostic session for user {}", userId);
        DiagnosticSessionResponse response = diagnosticService.getActiveSession(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/restart")
    @Operation(summary = "Restart diagnostic test", description = "Abandon any existing active session and start a new diagnostic test")
    public ResponseEntity<DiagnosticSessionResponse> restartDiagnostic(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody DiagnosticStartRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Restarting diagnostic test for user {}", userId);
        DiagnosticSessionResponse response = diagnosticService.restartDiagnostic(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/abandon/{sessionId}")
    @Operation(summary = "Abandon diagnostic session", description = "Mark an in-progress diagnostic session as abandoned")
    public ResponseEntity<Void> abandonDiagnostic(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Diagnostic session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Abandoning diagnostic test {} for user {}", sessionId, userId);
        diagnosticService.abandonDiagnostic(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{sessionId}")
    @Operation(summary = "Get diagnostic status", description = "Get current status of a diagnostic test")
    public ResponseEntity<DiagnosticSessionResponse> getDiagnosticStatus(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Diagnostic session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        DiagnosticSessionResponse response = diagnosticService.getDiagnosticStatus(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/result/{sessionId}")
    @Operation(summary = "Get diagnostic result", description = "Get result of a completed diagnostic test")
    public ResponseEntity<DiagnosticResultResponse> getDiagnosticResult(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Diagnostic session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting diagnostic result {} for user {}", sessionId, userId);
        DiagnosticResultResponse response = diagnosticService.getDiagnosticResult(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get diagnostic history", description = "Get list of user's completed diagnostic tests")
    public ResponseEntity<DiagnosticHistoryResponse> getDiagnosticHistory(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting diagnostic history for user {}", userId);
        DiagnosticHistoryResponse response = diagnosticService.getDiagnosticHistory(userId, page, size);
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
