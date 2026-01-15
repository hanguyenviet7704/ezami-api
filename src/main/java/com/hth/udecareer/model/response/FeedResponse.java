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
@Schema(description = "Feed/Post information")
public class FeedResponse {

    @Schema(description = "Feed ID", example = "4")
    private Long id;

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Feed title", example = "Đẹp")
    private String title;

    @Schema(description = "Feed slug", example = "dep-1764559565")
    private String slug;

    @Schema(description = "Feed message (raw)", example = "mọi người ơi trang web ezami đẹp quá")
    private String message;

    @Schema(description = "Feed message (rendered HTML)", example = "<p>mọi người ơi trang web ezami đẹp quá</p>")
    @JsonProperty("message_rendered")
    private String messageRendered;

    @Schema(description = "Feed type", example = "text")
    private String type;

    @Schema(description = "Content type", example = "text")
    @JsonProperty("content_type")
    private String contentType;

    @Schema(description = "Space ID", example = "2")
    @JsonProperty("space_id")
    private Long spaceId;

    @Schema(description = "Privacy", example = "public")
    private String privacy;

    @Schema(description = "Status", example = "published")
    private String status;

    @Schema(description = "Featured image URL", example = "https://example.com/image.jpg")
    @JsonProperty("featured_image")
    private String featuredImage;

    @Schema(description = "Comments count", example = "2")
    @JsonProperty("comments_count")
    private Integer commentsCount;

    @Schema(description = "Reactions count", example = "2")
    @JsonProperty("reactions_count")
    private Integer reactionsCount;

    @Schema(description = "Is sticky", example = "0")
    @JsonProperty("is_sticky")
    private Integer isSticky;

    @Schema(description = "Priority", example = "0")
    private Integer priority;

    @Schema(description = "Created at", example = "2025-12-01 10:26:05")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at", example = "2025-12-01 10:50:58")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "User profile")
    private XProfileResponse xprofile;

    @Schema(description = "Space information")
    private SpaceInfoResponse space;

    @Schema(description = "Comments list")
    @Builder.Default
    private List<CommentResponse> comments = new ArrayList<>();

    @Schema(description = "Media/Images list")
    @Builder.Default
    private List<MediaResponse> media = new ArrayList<>();

    @Schema(description = "User interactions")
    private FeedInteractionsResponse interactions;

    @Schema(description = "Comment like IDs")
    @JsonProperty("comment_like_ids")
    @Builder.Default
    private List<Long> commentLikeIds = new ArrayList<>();
}

