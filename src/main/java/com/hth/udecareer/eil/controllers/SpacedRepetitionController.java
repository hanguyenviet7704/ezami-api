package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.request.SrsCardRequest;
import com.hth.udecareer.eil.model.response.SrsCardResponse;
import com.hth.udecareer.eil.service.SpacedRepetitionService;
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
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/srs")
@Tag(name = "EIL Spaced Repetition", description = "Spaced Repetition System (SRS) APIs using SM-2 algorithm")
public class SpacedRepetitionController {

    private final SpacedRepetitionService srsService;
    private final UserRepository userRepository;

    @PostMapping("/cards")
    @Operation(summary = "Create SRS card", description = "Create a new SRS card for a question")
    public ResponseEntity<ApiResponse> createCard(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody SrsCardRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Creating SRS card for user {} question {}", userId, request.getQuestionId());

        SrsCardResponse response = srsService.createCard(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cards/bulk")
    @Operation(summary = "Bulk create SRS cards", description = "Create multiple SRS cards at once")
    public ResponseEntity<ApiResponse> bulkCreateCards(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody SrsCardRequest.BulkCreateRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Bulk creating {} SRS cards for user {}",
                request.getCards() != null ? request.getCards().size() : 0, userId);

        List<SrsCardResponse> response = srsService.bulkCreateCards(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cards/due")
    @Operation(summary = "Get due cards", description = "Get cards that are due for review")
    public ResponseEntity<ApiResponse> getDueCards(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting due SRS cards for user {}", userId);

        Page<SrsCardResponse> response = srsService.getDueCards(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cards")
    @Operation(summary = "Get all cards", description = "Get all SRS cards for user")
    public ResponseEntity<ApiResponse> getCards(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by certification") @RequestParam(required = false) String certificationCode,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting SRS cards for user {}", userId);

        Page<SrsCardResponse> response = srsService.getCards(userId, status, certificationCode, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cards/{cardId}")
    @Operation(summary = "Get card", description = "Get a specific SRS card")
    public ResponseEntity<ApiResponse> getCard(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Card ID") @PathVariable Long cardId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting SRS card {} for user {}", cardId, userId);

        SrsCardResponse response = srsService.getCard(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cards/{cardId}/review")
    @Operation(summary = "Record review", description = "Record a review using SM-2 algorithm (quality 0-5)")
    public ResponseEntity<ApiResponse> recordReview(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Card ID") @PathVariable Long cardId,
            @Valid @RequestBody SrsCardRequest.ReviewRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Recording review for card {} quality {} user {}", cardId, request.getQuality(), userId);

        SrsCardResponse.ReviewResult response = srsService.recordReview(userId, cardId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cards/{cardId}/suspend")
    @Operation(summary = "Suspend card", description = "Suspend a card from review schedule")
    public ResponseEntity<ApiResponse> suspendCard(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Card ID") @PathVariable Long cardId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Suspending SRS card {} for user {}", cardId, userId);

        SrsCardResponse response = srsService.updateCardStatus(userId, cardId, "SUSPENDED");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cards/{cardId}/resume")
    @Operation(summary = "Resume card", description = "Resume a suspended card")
    public ResponseEntity<ApiResponse> resumeCard(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Card ID") @PathVariable Long cardId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Resuming SRS card {} for user {}", cardId, userId);

        SrsCardResponse response = srsService.updateCardStatus(userId, cardId, "REVIEW");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/cards/{cardId}")
    @Operation(summary = "Delete card", description = "Delete an SRS card")
    public ResponseEntity<ApiResponse> deleteCard(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Card ID") @PathVariable Long cardId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Deleting SRS card {} for user {}", cardId, userId);

        srsService.deleteCard(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/stats")
    @Operation(summary = "Get SRS stats", description = "Get spaced repetition statistics")
    public ResponseEntity<ApiResponse> getStats(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting SRS stats for user {}", userId);

        SrsCardResponse.SrsStats response = srsService.getStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync cards", description = "Sync SRS cards between client and server")
    public ResponseEntity<ApiResponse> syncCards(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody SrsCardRequest.SyncRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Syncing SRS cards for user {}", userId);

        SrsCardResponse.SyncResponse response = srsService.syncCards(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
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
