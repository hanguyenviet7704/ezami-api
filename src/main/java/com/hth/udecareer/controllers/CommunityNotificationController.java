package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.CommunityNotificationResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UnreadCountResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.CommunityNotificationService;
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
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Community Notification APIs")
public class CommunityNotificationController {

    private final CommunityNotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Get notifications",
            description = "Get paginated list of notifications for the current user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/notifications")
    public ResponseEntity<PageResponse<CommunityNotificationResponse>> getNotifications(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @Parameter(description = "Only show unread notifications", example = "false")
            @RequestParam(value = "unread_only", required = false) Boolean unreadOnly) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        PageResponse<CommunityNotificationResponse> response = notificationService.getNotifications(userId, page, size, unreadOnly);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked as read",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityNotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<CommunityNotificationResponse> markAsRead(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Notification ID", example = "1")
            @PathVariable("id") Long notificationId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        CommunityNotificationResponse response = notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for the current user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked all as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Marked " + updated + " notifications as read",
                "updated_count", updated
        ));
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Get the count of unread notifications for the current user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved unread count",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnreadCountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
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
