package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.StreakService;
import com.hth.udecareer.service.StreakGoalService;
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

import java.security.Principal;
import java.util.List;

@Slf4j
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Streak", description = "Streak and Goal Management APIs")
public class StreakController {

    private final StreakService streakService;
    private final StreakGoalService streakGoalService;
    private final UserRepository userRepository;

    // ============= STREAK ENDPOINTS =============

    @Operation(
            summary = "Get current streak",
            description = "Get current user's streak data including current streak, longest streak, and freeze count.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved streak data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrentStreakResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/streak/current")
    public ResponseEntity<CurrentStreakResponse> getCurrentStreak(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        CurrentStreakResponse response = streakService.getCurrentStreak(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update streak progress",
            description = "Update streak when user performs any activity. Call this endpoint when user logs in or performs any tracked activity.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Streak updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StreakUpdateResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/streak/update")
    public ResponseEntity<StreakUpdateResponse> updateStreak(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        StreakUpdateResponse response = streakService.updateStreak(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get streak statistics",
            description = "Get detailed statistics including rank, total active days, and freeze usage.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StreakStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/streak/stats")
    public ResponseEntity<StreakStatsResponse> getStreakStats(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        StreakStatsResponse response = streakService.getStreakStats(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Use streak freeze",
            description = "Manually use a streak freeze (for future feature). Currently freezes are auto-used.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freeze used successfully"),
            @ApiResponse(responseCode = "400", description = "No freeze available or not supported"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/streak/freeze")
    public ResponseEntity<StreakFreezeResponse> useStreakFreeze(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        StreakFreezeResponse response = streakService.useStreakFreeze(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get streak leaderboard",
            description = "Get top users with longest streaks. Public endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leaderboard",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StreakLeaderboardResponse.class)))
    })
    @GetMapping("/streak/leaderboard")
    public ResponseEntity<StreakLeaderboardResponse> getLeaderboard(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        StreakLeaderboardResponse response = streakService.getLeaderboard(page, size);
        return ResponseEntity.ok(response);
    }

    // ============= GOAL ENDPOINTS =============

    @Operation(
            summary = "Get available goals",
            description = "Get all available streak goals with user's progress.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved goals"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/streak/goals")
    public ResponseEntity<List<StreakGoalResponse>> getAvailableGoals(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        List<StreakGoalResponse> response = streakGoalService.getAvailableGoals(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Claim goal reward",
            description = "Claim rewards for a completed goal.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reward claimed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GoalClaimResponse.class))),
            @ApiResponse(responseCode = "400", description = "Goal not completed or already claimed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PostMapping("/streak/claim-goal")
    public ResponseEntity<GoalClaimResponse> claimGoalReward(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Goal ID", example = "1")
            @RequestParam("goalId") Long goalId) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        GoalClaimResponse response = streakGoalService.claimGoalReward(userId, goalId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Adjust streak goals",
            description = "Enable or disable specific streak goals for the user. Allows customization of which goals to track.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goals adjusted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PostMapping("/streak/goals/adjust")
    public ResponseEntity<List<StreakGoalResponse>> adjustGoals(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Map of goal ID to enabled status (true = enable, false = disable)",
                    example = "{\"1\": true, \"2\": false}")
            @RequestBody java.util.Map<Long, Boolean> goalPreferences) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        List<StreakGoalResponse> response = streakGoalService.adjustGoals(userId, goalPreferences);
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
