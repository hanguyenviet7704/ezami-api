package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.TopicRequest;
import com.hth.udecareer.model.response.TopicResponse;
import com.hth.udecareer.service.TopicService;
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
import java.util.List;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Topic", description = "Topic APIs - Post Topics Management")
public class TopicController {

    private final TopicService topicService;

    // ============= PUBLIC ENDPOINTS =============

    @Operation(
            summary = "Get all topics",
            description = "Get a list of all available post topics with feed counts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved topics list")
    })
    @GetMapping("/topics")
    public ResponseEntity<List<TopicResponse>> getAllTopics() {
        List<TopicResponse> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @Operation(
            summary = "Get topic by ID",
            description = "Get a specific topic by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved topic",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TopicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    @GetMapping("/topics/{id}")
    public ResponseEntity<TopicResponse> getTopicById(
            @Parameter(description = "Topic ID", example = "1")
            @PathVariable("id") Long id) {
        TopicResponse topic = topicService.getTopicById(id);
        return ResponseEntity.ok(topic);
    }

    @Operation(
            summary = "Get topic by slug",
            description = "Get a specific topic by its slug."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved topic",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TopicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    @GetMapping("/topics/by-slug/{slug}")
    public ResponseEntity<TopicResponse> getTopicBySlug(
            @Parameter(description = "Topic slug", example = "general-discussion")
            @PathVariable("slug") String slug) {
        TopicResponse topic = topicService.getTopicBySlug(slug);
        return ResponseEntity.ok(topic);
    }

    // ============= ADMIN ENDPOINTS =============

    @Operation(
            summary = "Get all topics (Admin)",
            description = "Get all topics with statistics for admin management.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved topics list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/admin/topics")
    public ResponseEntity<List<TopicResponse>> getAdminTopics() {
        List<TopicResponse> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @Operation(
            summary = "Create or update topic (Admin)",
            description = "Create a new topic or update an existing one. Provide ID for update.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully saved topic",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TopicResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Topic not found (for update)")
    })
    @PostMapping("/admin/topics")
    public ResponseEntity<TopicResponse> saveTopic(
            @Valid @RequestBody TopicRequest request) {
        TopicResponse topic = topicService.saveTopic(request);
        return ResponseEntity.ok(topic);
    }

    @Operation(
            summary = "Delete topic (Admin)",
            description = "Delete a topic by its ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted topic"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    @DeleteMapping("/admin/topics/{id}")
    public ResponseEntity<Void> deleteTopic(
            @Parameter(description = "Topic ID", example = "1")
            @PathVariable("id") Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.ok().build();
    }
}
