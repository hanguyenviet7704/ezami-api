package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.dto.EzNotificationDto;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.service.EzNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Ezami Notifications", description = "App-level notifications stored in ez_notifications")
@SecurityRequirement(name = "bearerAuth")
public class EzNotificationController {

    private final EzNotificationService ezNotificationService;

    @GetMapping("/ez-notifications/my")
    @Operation(summary = "List my app notifications", description = "List notifications from ez_notifications (supports pagination and unreadOnly)")
    public ResponseEntity<PageResponse<EzNotificationDto>> getMyNotifications(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        return ResponseEntity.ok(ezNotificationService.getMyNotifications(principal, page, size, unreadOnly));
    }

    @PostMapping("/ez-notifications/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ApiResponse markRead(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("id") Long id
    ) {
        ezNotificationService.markRead(principal, id);
        return ApiResponse.success();
    }

    @PostMapping("/ez-notifications/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ApiResponse markAllRead(@Parameter(hidden = true) Principal principal) {
        int updated = ezNotificationService.markAllRead(principal);
        return ApiResponse.success(Map.of("updated", updated));
    }
}

