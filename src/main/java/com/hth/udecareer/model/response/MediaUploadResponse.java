package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Media upload response")
public class MediaUploadResponse {

    @Schema(description = "Media information")
    private MediaInfo media;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInfo {
        @Schema(description = "Media URL", example = "http://example.com/wp-content/uploads/...")
        private String url;

        @Schema(description = "Media key", example = "abc123...")
        @JsonProperty("media_key")
        private String mediaKey;

        @Schema(description = "Media type", example = "image/webp")
        private String type;

        @Schema(description = "Image width", example = "1400")
        private Integer width;

        @Schema(description = "Image height", example = "697")
        private Integer height;
    }
}

