package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a feed")
public class FeedUpdateRequest {

    @Schema(description = "Feed content/message", example = "Updated message")
    private String message;

    @Schema(description = "Space slug", example = "start-here")
    private String space;

    @Schema(description = "Feed title", example = "Updated Title")
    private String title;

    @Schema(description = "Content type", example = "text")
    private String contentType;

    @Schema(description = "Privacy setting", example = "public")
    private String privacy;

    @Schema(description = "List of media items with media keys")
    private List<FeedCreateRequest.MediaItemRequest> mediaItems;
}

