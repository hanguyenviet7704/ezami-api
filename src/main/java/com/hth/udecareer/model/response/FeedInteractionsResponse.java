package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "User interactions with feed")
public class FeedInteractionsResponse {

    @Schema(description = "User liked this feed", example = "false")
    private Boolean like;

    @Schema(description = "User bookmarked this feed", example = "false")
    private Boolean bookmark;
}

