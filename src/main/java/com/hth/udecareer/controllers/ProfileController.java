package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.FollowItemResponse;
import com.hth.udecareer.model.response.FollowResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.FollowService;
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

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Profile APIs - Follow, Block, and User Relationships")
public class ProfileController {

    private final FollowService followService;
    private final UserRepository userRepository;

    // ============= PROFILE ENDPOINTS =============

    @Operation(
            summary = "Get user profile by username",
            description = "Get public profile information for a user by their username. Includes follow counts and relationship status if authenticated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = XProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile/{username}")
    public ResponseEntity<XProfileResponse> getUserProfile(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user", example = "john_doe")
            @PathVariable("username") String username) throws AppException {

        Long currentUserId = getUserIdFromPrincipalOptional(principal);
        XProfileResponse profile = followService.getPublicProfile(username, currentUserId);
        return ResponseEntity.ok(profile);
    }

    // ============= FOLLOW ENDPOINTS =============

    @Operation(
            summary = "Follow a user",
            description = "Follow a user by their username. Creates a follow relationship.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully followed user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., trying to follow yourself)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/profile/{username}/follow")
    public ResponseEntity<FollowResponse> followUser(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user to follow", example = "john_doe")
            @PathVariable("username") String username) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal);
        FollowResponse response = followService.follow(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Unfollow a user",
            description = "Unfollow a user by their username. Removes the follow relationship.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unfollowed user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/profile/{username}/unfollow")
    public ResponseEntity<FollowResponse> unfollowUser(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user to unfollow", example = "john_doe")
            @PathVariable("username") String username) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal);
        FollowResponse response = followService.unfollow(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    // ============= BLOCK ENDPOINTS =============

    @Operation(
            summary = "Block a user",
            description = "Block a user by their username. The blocked user won't be able to interact with you.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully blocked user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., trying to block yourself)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/profile/{username}/block")
    public ResponseEntity<FollowResponse> blockUser(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user to block", example = "john_doe")
            @PathVariable("username") String username) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal);
        FollowResponse response = followService.block(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Unblock a user",
            description = "Unblock a user by their username. Restores normal interaction with the user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unblocked user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/profile/{username}/unblock")
    public ResponseEntity<FollowResponse> unblockUser(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user to unblock", example = "john_doe")
            @PathVariable("username") String username) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal);
        FollowResponse response = followService.unblock(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    // ============= NOTIFICATION TOGGLE =============

    @Operation(
            summary = "Toggle notifications for a followed user",
            description = "Enable or disable notifications for a user you follow. You must be following the user first.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully toggled notifications",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "400", description = "You must follow the user first"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found or not following")
    })
    @PostMapping("/profile/{username}/notification")
    public ResponseEntity<FollowResponse> toggleNotification(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the followed user", example = "john_doe")
            @PathVariable("username") String username,
            @Parameter(description = "Enable notifications (true) or disable (false)", example = "true")
            @RequestParam(value = "enable", required = false, defaultValue = "true") Boolean enable) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal);
        FollowResponse response = followService.toggleNotification(currentUserId, username, enable);
        return ResponseEntity.ok(response);
    }

    // ============= LIST ENDPOINTS =============

    @Operation(
            summary = "Get followers of a user",
            description = "Get a paginated list of users who follow the specified user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved followers list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile/{username}/followers")
    public ResponseEntity<PageResponse<FollowItemResponse>> getFollowers(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user", example = "john_doe")
            @PathVariable("username") String username,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) throws AppException {

        Long currentUserId = getUserIdFromPrincipalOptional(principal);
        PageResponse<FollowItemResponse> response = followService.getFollowers(username, currentUserId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get users that a user is following",
            description = "Get a paginated list of users that the specified user is following."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved followings list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile/{username}/followings")
    public ResponseEntity<PageResponse<FollowItemResponse>> getFollowings(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Username of the user", example = "john_doe")
            @PathVariable("username") String username,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) throws AppException {

        Long currentUserId = getUserIdFromPrincipalOptional(principal);
        PageResponse<FollowItemResponse> response = followService.getFollowings(username, currentUserId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get blocked users",
            description = "Get a paginated list of users that the current user has blocked.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved blocked users list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping("/profile/blocked-users")
    public ResponseEntity<PageResponse<FollowItemResponse>> getBlockedUsers(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal);
        PageResponse<FollowItemResponse> response = followService.getBlockedUsers(currentUserId, page, size);
        return ResponseEntity.ok(response);
    }

    // ============= PRIVATE HELPER METHODS =============

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

    private Long getUserIdFromPrincipalOptional(Principal principal) {
        if (principal == null) {
            return null;
        }

        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }
}
