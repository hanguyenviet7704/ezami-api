package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Feed bookmark response")
public class FeedBookmarkResponse {

    @Schema(description = "Feed ID", example = "15")
    private Long feedId;

    @Schema(description = "Is bookmarked", example = "true")
    private Boolean bookmarked;

    @Schema(description = "Message", example = "Feed bookmarked successfully")
    private String message;
}
