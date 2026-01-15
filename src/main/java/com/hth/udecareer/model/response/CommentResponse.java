package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comment information")
public class CommentResponse {

    @Schema(description = "Comment ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "16483")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Post ID", example = "4")
    @JsonProperty("post_id")
    private Long postId;

    @Schema(description = "Parent comment ID (null if root comment)", example = "null")
    @JsonProperty("parent_id")
    private Long parentId;

    @Schema(description = "Comment message", example = "đẹp thật à")
    private String message;

    @Schema(description = "Rendered message", example = "<p>đẹp thật à</p>")
    @JsonProperty("message_rendered")
    private String messageRendered;

    @Schema(description = "Reactions count", example = "0")
    @JsonProperty("reactions_count")
    private Integer reactionsCount;

    @Schema(description = "Number of child comments (replies)", example = "5")
    @JsonProperty("replies_count")
    @Builder.Default
    private Long repliesCount = 0L;

    @Schema(description = "Type", example = "comment")
    private String type;

    @Schema(description = "Content type", example = "text")
    @JsonProperty("content_type")
    private String contentType;

    @Schema(description = "Status", example = "published")
    private String status;

    @Schema(description = "Created at", example = "2025-12-01 10:50:33")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at", example = "2025-12-01 10:50:33")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "User profile")
    private XProfileResponse xprofile;

    @Schema(description = "User liked this comment (0 or 1)", example = "0")
    private Integer liked;

    @Schema(description = "Media/Images list")
    @Builder.Default
    private List<MediaResponse> media = new ArrayList<>();

    @Schema(description = "Child comments (replies) list")
    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();
}

