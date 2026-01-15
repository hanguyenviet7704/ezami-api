package com.hth.udecareer.controllers;


import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.CommentDto;
import com.hth.udecareer.model.request.CommentRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.CommentRepository;
import com.hth.udecareer.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Get root comments for a post", description = "Retrieve all root comments (parent = 0) for a specific post by postId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Root comments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content)
    })

    @GetMapping("/root")
    public ResponseEntity<PageResponse<CommentDto>> getAllComments(@RequestParam("postId") Long postId,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "5") int size) {
        PageResponse<CommentDto> rootComment = commentService.getRootComments(postId, page, size);
        return ResponseEntity.ok(rootComment);
    }


    @Operation(summary = "Get replies of a comment", description = "Retrieve all direct replies of a comment by commentId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replies retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content),
            @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content)
    })
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<PageResponse<CommentDto>> getAllCommentsByCommentId(@PathVariable("commentId") Long commentId,
                                                                              @RequestParam(value = "page", defaultValue = "0") int page,
                                                                              @RequestParam(value = "size", defaultValue = "5") int size) {
        PageResponse<CommentDto> replies = commentService.getReplies(commentId, page, size);
        return ResponseEntity.ok(replies);
    }


    @Operation(summary = "Create a new comment", description = "Create a new comment on a post. Requires authentication (JWT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<CommentDto> createComment(Principal principal, @Valid @RequestBody CommentRequest commentRequest) throws AppException {
        CommentDto created = commentService.createComment(commentRequest, principal.getName());
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Delete a comment", description = "Delete a comment by commentId. Requires authentication (JWT). Only author can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment deleted successfully, no content returned"),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - user is not the author", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT", content = @Content)
    })

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(Principal principal, @PathVariable("commentId") Long commentId) throws AppException {
        commentService.deleteComment(commentId, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }

}
