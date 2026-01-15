package com.hth.udecareer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new feed")
public class FeedCreateRequest {

    @NotBlank(message = "Message is required")
    @Schema(description = "Feed content/message", example = "mọi người ơi trang web ezami đẹp quá", required = true)
    private String message;

    @Schema(description = "Space slug", example = "start-here")
    private String space;

    @Schema(description = "Feed title (optional)", example = "Đẹp")
    private String title;

    @Schema(description = "Content type", example = "text", allowableValues = {"text", "image", "video"})
    private String contentType;

    @Schema(description = "Privacy setting", example = "public", allowableValues = {"public", "private"})
    private String privacy;

    @Schema(description = "List of media items with media keys", example = "[{\"media_key\": \"abc123...\"}]")
    private List<MediaItemRequest> mediaItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItemRequest {
        @Schema(description = "Media key from upload", example = "abc123...")
        @JsonProperty("media_key")
        private String mediaKey;
    }
}

