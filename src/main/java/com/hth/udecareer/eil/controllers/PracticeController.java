package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.request.PracticeStartRequest;
import com.hth.udecareer.eil.model.request.PracticeSubmitRequest;
import com.hth.udecareer.eil.model.response.NextQuestionResponse;
import com.hth.udecareer.eil.model.response.PracticeResultResponse;
import com.hth.udecareer.eil.model.response.PracticeSessionResponse;
import com.hth.udecareer.eil.service.PracticeService;
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
@RequestMapping("/api/eil/practice")
@Tag(name = "EIL Practice", description = "Adaptive practice session APIs")
public class PracticeController {

    private final PracticeService practiceService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    @Operation(summary = "Start practice session", description = "Start a new adaptive practice session")
    public ResponseEntity<PracticeSessionResponse> startPractice(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody PracticeStartRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Starting practice session for user {}, type={}", userId, request.getSessionType());
        PracticeSessionResponse response = practiceService.startPractice(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/next-question/{sessionId}")
    @Operation(summary = "Get next question (GET)", description = "Get the next question for the practice session via GET")
    public ResponseEntity<NextQuestionResponse> getNextQuestionByPath(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Practice session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting next question (GET) for user {}, session {}", userId, sessionId);
        NextQuestionResponse response = practiceService.getNextQuestion(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/next-question")
    @Operation(summary = "Get next question (POST)", description = "Get the next question for the practice session via POST")
    public ResponseEntity<NextQuestionResponse> getNextQuestion(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Practice session ID") @RequestParam String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting next question (POST) for user {}, session {}", userId, sessionId);
        NextQuestionResponse response = practiceService.getNextQuestion(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit answer", description = "Submit an answer for a practice question")
    public ResponseEntity<PracticeResultResponse> submitAnswer(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody PracticeSubmitRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Submitting practice answer for user {}, session {}", userId, request.getSessionId());
        PracticeResultResponse response = practiceService.submitAnswer(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end/{sessionId}")
    @Operation(summary = "End practice session", description = "End the current practice session")
    public ResponseEntity<PracticeSessionResponse> endSession(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Practice session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Ending practice session {} for user {}", sessionId, userId);
        PracticeSessionResponse response = practiceService.endSession(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{sessionId}")
    @Operation(summary = "Get session status", description = "Get current status of a practice session")
    public ResponseEntity<PracticeSessionResponse> getSessionStatus(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Practice session ID") @PathVariable String sessionId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        PracticeSessionResponse response = practiceService.getSessionStatus(userId, sessionId);
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
