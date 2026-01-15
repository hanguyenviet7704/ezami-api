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
@Schema(description = "Media information in feed")
public class MediaResponse {

    @Schema(description = "Media ID", example = "1")
    private Long id;

    @Schema(description = "Media key", example = "abc123-def456")
    @JsonProperty("media_key")
    private String mediaKey;

    @Schema(description = "Media URL", example = "/uploads/abc123.jpg")
    private String url;

    @Schema(description = "Media type", example = "image/jpeg")
    private String type;

    @Schema(description = "Image width", example = "1920")
    private Integer width;

    @Schema(description = "Image height", example = "1080")
    private Integer height;
}

