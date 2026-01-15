package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ScheduledPostRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.ScheduledPostResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.ScheduledPostService;
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
@Tag(name = "Scheduled Post", description = "Scheduled Post APIs")
public class ScheduledPostController {

    private final ScheduledPostService scheduledPostService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Create scheduled post",
            description = "Schedule a post to be published at a future time.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduled post created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledPostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/scheduled-posts")
    public ResponseEntity<ScheduledPostResponse> createScheduledPost(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody ScheduledPostRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        ScheduledPostResponse response = scheduledPostService.createScheduledPost(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get my scheduled posts",
            description = "Get list of scheduled posts for the current user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved scheduled posts"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/scheduled-posts")
    public ResponseEntity<PageResponse<ScheduledPostResponse>> getScheduledPosts(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Filter by status: scheduled, published, cancelled, failed")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        PageResponse<ScheduledPostResponse> response = scheduledPostService.getScheduledPosts(userId, status, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get upcoming scheduled posts",
            description = "Get list of posts scheduled for the future.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved upcoming posts"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/scheduled-posts/upcoming")
    public ResponseEntity<PageResponse<ScheduledPostResponse>> getUpcomingPosts(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        PageResponse<ScheduledPostResponse> response = scheduledPostService.getUpcomingPosts(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get scheduled post by ID",
            description = "Get a specific scheduled post by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved scheduled post",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledPostResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Scheduled post not found")
    })
    @GetMapping("/scheduled-posts/{id}")
    public ResponseEntity<ScheduledPostResponse> getScheduledPostById(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Scheduled post ID", example = "1")
            @PathVariable("id") Long postId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        ScheduledPostResponse response = scheduledPostService.getScheduledPostById(userId, postId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update scheduled post",
            description = "Update a scheduled post (only if not yet published).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduled post updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledPostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or post already published"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Scheduled post not found")
    })
    @PutMapping("/scheduled-posts/{id}")
    public ResponseEntity<ScheduledPostResponse> updateScheduledPost(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Scheduled post ID", example = "1")
            @PathVariable("id") Long postId,
            @Valid @RequestBody ScheduledPostRequest request) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        ScheduledPostResponse response = scheduledPostService.updateScheduledPost(userId, postId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancel scheduled post",
            description = "Cancel a scheduled post (only if not yet published).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduled post cancelled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledPostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Post already published"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Scheduled post not found")
    })
    @PostMapping("/scheduled-posts/{id}/cancel")
    public ResponseEntity<ScheduledPostResponse> cancelScheduledPost(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Scheduled post ID", example = "1")
            @PathVariable("id") Long postId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        ScheduledPostResponse response = scheduledPostService.cancelScheduledPost(userId, postId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete scheduled post",
            description = "Delete a scheduled post (only if not published).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheduled post deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete published post"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Scheduled post not found")
    })
    @DeleteMapping("/scheduled-posts/{id}")
    public ResponseEntity<Map<String, Object>> deleteScheduledPost(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Scheduled post ID", example = "1")
            @PathVariable("id") Long postId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        scheduledPostService.deleteScheduledPost(userId, postId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Scheduled post deleted successfully"
        ));
    }

    @Operation(
            summary = "Get scheduled post counts",
            description = "Get counts of scheduled posts by status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved counts"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/scheduled-posts/counts")
    public ResponseEntity<Map<String, Long>> getScheduledPostCounts(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        return ResponseEntity.ok(Map.of(
                "scheduled", scheduledPostService.getScheduledPostCount(userId, "scheduled"),
                "published", scheduledPostService.getScheduledPostCount(userId, "published"),
                "cancelled", scheduledPostService.getScheduledPostCount(userId, "cancelled"),
                "failed", scheduledPostService.getScheduledPostCount(userId, "failed")
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
