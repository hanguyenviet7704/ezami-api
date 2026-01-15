package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.eil.model.request.SaveMockResultRequest;
import com.hth.udecareer.eil.model.response.MockResultResponse;
import com.hth.udecareer.eil.model.response.MockResultStatsResponse;
import com.hth.udecareer.eil.service.MockResultService;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "EIL Mock Test Results", description = "Mock test result management APIs")
public class MockResultController {

    private final MockResultService mockResultService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Save mock test result",
            description = "Save a completed mock test result with answers",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/eil/mock-results")
    public ResponseEntity<MockResultResponse> saveMockResult(
            @Parameter(hidden = true) Principal principal,
            @RequestBody SaveMockResultRequest request) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        MockResultResponse response = mockResultService.saveMockResult(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get latest mock result",
            description = "Get the most recent mock test result, optionally filtered by certificate code",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/eil/mock-results/latest")
    public ResponseEntity<MockResultResponse> getLatestResult(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(value = "certificateCode", required = false) String certificateCode) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        MockResultResponse response = mockResultService.getLatestResult(userId, certificateCode);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get mock result history",
            description = "Get paginated history of mock test results",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/eil/mock-results/history")
    public ResponseEntity<Page<MockResultResponse>> getResultHistory(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(value = "certificateCode", required = false) String certificateCode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        Page<MockResultResponse> response = mockResultService.getResultHistory(userId, certificateCode, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get mock result by ID",
            description = "Get detailed mock test result by its ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/eil/mock-results/{id}")
    public ResponseEntity<MockResultResponse> getResultById(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("id") Long id) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        MockResultResponse response = mockResultService.getResultById(userId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get mock result statistics",
            description = "Get statistics for a certificate's mock test results",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/eil/mock-results/stats")
    public ResponseEntity<MockResultStatsResponse> getStats(
            @Parameter(hidden = true) Principal principal,
            @RequestParam("certificateCode") String certificateCode) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        MockResultStatsResponse response = mockResultService.getStats(userId, certificateCode);
        return ResponseEntity.ok(response);
    }

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
