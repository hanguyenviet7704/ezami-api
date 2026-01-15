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
@Schema(description = "Request to create a comment or reply to a comment")
public class CommentCreateRequest {

    @NotBlank(message = "Message is required")
    @Schema(description = "Comment message content", example = "Đây là nội dung comment của tôi", required = true)
    private String message;

    @Schema(description = "Parent comment ID (for reply). If null or not provided, this is a root comment. If provided, this comment will be a reply to the parent comment", example = "123")
    private Long parentId;

    @Schema(description = "List of media items with media keys. These are the media keys returned from the upload media API. If empty array or null, no images will be attached", 
            example = "[{\"media_key\": \"abc123...\"}]")
    private List<MediaItemRequest> mediaImages;

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

