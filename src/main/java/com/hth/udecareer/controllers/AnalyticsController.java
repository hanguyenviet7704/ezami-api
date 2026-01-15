package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.response.AnalyticsActivityResponse;
import com.hth.udecareer.model.response.AnalyticsWidgetResponse;
import com.hth.udecareer.model.response.LeaderboardItemResponse;
import com.hth.udecareer.service.AnalyticsService;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs - Community Statistics (Admin)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ============= OVERVIEW =============

    @Operation(
            summary = "Get overview widget",
            description = "Get overview statistics including posts, comments, reactions, and active users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved overview stats",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalyticsWidgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/analytics/overview/widget")
    public ResponseEntity<AnalyticsWidgetResponse> getOverviewWidget(
            @Parameter(description = "Number of days to compare", example = "30")
            @RequestParam(value = "days", required = false, defaultValue = "30") int days) {

        AnalyticsWidgetResponse response = analyticsService.getOverviewWidget(days);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get activity chart",
            description = "Get activity data for charts (posts, comments, reactions, or users over time).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved activity data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalyticsActivityResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/analytics/overview/activity")
    public ResponseEntity<AnalyticsActivityResponse> getActivityChart(
            @Parameter(description = "Activity type: posts, comments, reactions, users", example = "posts")
            @RequestParam(value = "activity", required = false, defaultValue = "posts") String activity,
            @Parameter(description = "Number of days", example = "30")
            @RequestParam(value = "days", required = false, defaultValue = "30") int days) {

        AnalyticsActivityResponse response = analyticsService.getActivityChart(activity, days);
        return ResponseEntity.ok(response);
    }

    // ============= MEMBERS =============

    @Operation(
            summary = "Get members widget",
            description = "Get members statistics including total and active members.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members stats",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalyticsWidgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/analytics/members/widget")
    public ResponseEntity<AnalyticsWidgetResponse> getMembersWidget(
            @Parameter(description = "Number of days to compare", example = "30")
            @RequestParam(value = "days", required = false, defaultValue = "30") int days) {

        AnalyticsWidgetResponse response = analyticsService.getMembersWidget(days);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get top members",
            description = "Get list of top members by engagement (reactions received).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top members"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/analytics/members/top-members")
    public ResponseEntity<List<LeaderboardItemResponse>> getTopMembers(
            @Parameter(description = "Number of members to return", example = "10")
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {

        List<LeaderboardItemResponse> response = analyticsService.getTopMembers(limit);
        return ResponseEntity.ok(response);
    }

    // ============= SPACES =============

    @Operation(
            summary = "Get spaces widget",
            description = "Get spaces statistics.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved spaces stats",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalyticsWidgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/analytics/spaces/widget")
    public ResponseEntity<AnalyticsWidgetResponse> getSpacesWidget() {
        AnalyticsWidgetResponse response = analyticsService.getSpacesWidget();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get popular spaces",
            description = "Get list of most popular spaces by post count.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved popular spaces"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/analytics/spaces/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularSpaces(
            @Parameter(description = "Number of spaces to return", example = "10")
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {

        List<Map<String, Object>> response = analyticsService.getPopularSpaces(limit);
        return ResponseEntity.ok(response);
    }
}
