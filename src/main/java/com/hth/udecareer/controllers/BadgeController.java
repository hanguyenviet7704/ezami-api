package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.AwardBadgeRequest;
import com.hth.udecareer.model.request.BadgeRequest;
import com.hth.udecareer.model.response.BadgeResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UserBadgeResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.BadgeService;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Badge", description = "Badge APIs - Achievements and Rewards")
public class BadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    // ============= PUBLIC ENDPOINTS =============

    @Operation(
            summary = "Get all badges",
            description = "Get list of all active badges."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badges list")
    })
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    @Operation(
            summary = "Get badges by type",
            description = "Get badges filtered by type (achievement, milestone, special, custom)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badges"),
            @ApiResponse(responseCode = "400", description = "Invalid badge type")
    })
    @GetMapping("/badges/type/{type}")
    public ResponseEntity<List<BadgeResponse>> getBadgesByType(
            @Parameter(description = "Badge type: achievement, milestone, special, custom", example = "achievement")
            @PathVariable("type") String type) {
        List<BadgeResponse> badges = badgeService.getBadgesByType(type);
        return ResponseEntity.ok(badges);
    }

    @Operation(
            summary = "Get badge by ID",
            description = "Get a specific badge by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badge",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadgeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Badge not found")
    })
    @GetMapping("/badges/{id}")
    public ResponseEntity<BadgeResponse> getBadgeById(
            @Parameter(description = "Badge ID", example = "1")
            @PathVariable("id") Long badgeId) {
        BadgeResponse badge = badgeService.getBadgeById(badgeId);
        return ResponseEntity.ok(badge);
    }

    @Operation(
            summary = "Get badge by slug",
            description = "Get a specific badge by its slug."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badge",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadgeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Badge not found")
    })
    @GetMapping("/badges/by-slug/{slug}")
    public ResponseEntity<BadgeResponse> getBadgeBySlug(
            @Parameter(description = "Badge slug", example = "first-post")
            @PathVariable("slug") String slug) {
        BadgeResponse badge = badgeService.getBadgeBySlug(slug);
        return ResponseEntity.ok(badge);
    }

    @Operation(
            summary = "Get badge earners",
            description = "Get list of users who earned a specific badge."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badge earners"),
            @ApiResponse(responseCode = "404", description = "Badge not found")
    })
    @GetMapping("/badges/{id}/earners")
    public ResponseEntity<PageResponse<UserBadgeResponse>> getBadgeEarners(
            @Parameter(description = "Badge ID", example = "1")
            @PathVariable("id") Long badgeId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        PageResponse<UserBadgeResponse> earners = badgeService.getBadgeEarners(badgeId, page, size);
        return ResponseEntity.ok(earners);
    }

    // ============= USER ENDPOINTS =============

    @Operation(
            summary = "Get my badges",
            description = "Get list of badges earned by the current user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user badges"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping({"/badges/my-badges", "/badges/me"})
    public ResponseEntity<List<UserBadgeResponse>> getMyBadges(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        List<UserBadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    @Operation(
            summary = "Get user's badges",
            description = "Get list of badges earned by a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user badges")
    })
    @GetMapping("/badges/user/{userId}")
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(
            @Parameter(description = "User ID", example = "100")
            @PathVariable("userId") Long userId) {
        List<UserBadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    @Operation(
            summary = "Get featured badges",
            description = "Get user's featured badges for display on profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved featured badges"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/badges/featured")
    public ResponseEntity<List<UserBadgeResponse>> getFeaturedBadges(
            @Parameter(hidden = true) Principal principal) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        List<UserBadgeResponse> badges = badgeService.getFeaturedBadges(userId);
        return ResponseEntity.ok(badges);
    }

    @Operation(
            summary = "Set badge as featured",
            description = "Set or unset a badge as featured on your profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge feature status updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBadgeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Badge not found or not earned")
    })
    @PostMapping("/badges/{id}/featured")
    public ResponseEntity<UserBadgeResponse> setFeaturedBadge(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Badge ID", example = "1")
            @PathVariable("id") Long badgeId,
            @Parameter(description = "Set as featured", example = "true")
            @RequestParam(value = "featured", required = false, defaultValue = "true") boolean featured) throws AppException {
        Long userId = getUserIdFromPrincipal(principal);
        UserBadgeResponse response = badgeService.setFeaturedBadge(userId, badgeId, featured);
        return ResponseEntity.ok(response);
    }

    // ============= ADMIN ENDPOINTS =============

    @Operation(
            summary = "Get all badges (Admin)",
            description = "Get all badges including inactive ones.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badges"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/admin/badges")
    public ResponseEntity<PageResponse<BadgeResponse>> getAllBadgesAdmin(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        PageResponse<BadgeResponse> badges = badgeService.getAllBadgesAdmin(page, size);
        return ResponseEntity.ok(badges);
    }

    @Operation(
            summary = "Create or update badge (Admin)",
            description = "Create a new badge or update an existing one.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge saved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadgeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/admin/badges")
    public ResponseEntity<BadgeResponse> saveBadge(
            @Valid @RequestBody BadgeRequest request) {
        BadgeResponse badge = badgeService.saveBadge(request);
        return ResponseEntity.ok(badge);
    }

    @Operation(
            summary = "Delete badge (Admin)",
            description = "Delete a badge by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Badge not found")
    })
    @DeleteMapping("/admin/badges/{id}")
    public ResponseEntity<Map<String, Object>> deleteBadge(
            @Parameter(description = "Badge ID", example = "1")
            @PathVariable("id") Long badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Badge deleted successfully"
        ));
    }

    @Operation(
            summary = "Award badge to user (Admin)",
            description = "Manually award a badge to a user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge awarded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserBadgeResponse.class))),
            @ApiResponse(responseCode = "400", description = "User already has this badge"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User or badge not found")
    })
    @PostMapping("/admin/badges/award")
    public ResponseEntity<UserBadgeResponse> awardBadge(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody AwardBadgeRequest request) throws AppException {
        Long awarderId = getUserIdFromPrincipal(principal);
        UserBadgeResponse response = badgeService.awardBadge(awarderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Revoke badge from user (Admin)",
            description = "Remove a badge from a user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge revoked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User doesn't have this badge")
    })
    @DeleteMapping("/admin/badges/user/{userId}/badge/{badgeId}")
    public ResponseEntity<Map<String, Object>> revokeBadge(
            @Parameter(description = "User ID", example = "100")
            @PathVariable("userId") Long userId,
            @Parameter(description = "Badge ID", example = "1")
            @PathVariable("badgeId") Long badgeId) {
        badgeService.revokeBadge(userId, badgeId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Badge revoked successfully"
        ));
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
