package com.hth.udecareer.model.request;

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
@Schema(description = "Request to update a comment")
public class CommentUpdateRequest {

    @NotBlank(message = "Message is required")
    @Schema(description = "Updated comment message content", example = "Nội dung comment đã được sửa", required = true)
    private String message;

    @Schema(description = """
            List of media items with media keys to replace existing media. 
            - If `null` or not provided: existing media will be preserved
            - If empty array `[]`: all existing media will be removed
            - If array with media items: existing media will be replaced with new media keys
            These are the media keys returned from the upload media API.
            """, 
            example = "[{\"media_key\": \"abc123...\"}]")
    private List<CommentCreateRequest.MediaItemRequest> mediaImages;
}

