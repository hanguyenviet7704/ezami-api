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
@Schema(description = "Feed reaction response")
public class FeedReactionResponse {

    @Schema(description = "Feed ID", example = "15")
    private Long feedId;

    @Schema(description = "Is liked", example = "true")
    private Boolean liked;

    @Schema(description = "Total reactions count", example = "5")
    private Long reactionsCount;
}

