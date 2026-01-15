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
@Schema(description = "Feed create/update response")
public class FeedCreateResponse {

    @Schema(description = "Feed information")
    private FeedResponse feed;

    @Schema(description = "Success message", example = "Your post has been published")
    private String message;

    @Schema(description = "Last fetched timestamp")
    @JsonProperty("last_fetched_timestamp")
    private Long lastFetchedTimestamp;
}

