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
@Schema(description = "Feed list response with pagination")
public class FeedListResponse {

    @Schema(description = "Feeds data with pagination")
    private PageResponse<FeedResponse> feeds;

    @Schema(description = "Sticky feed (only on page 1)")
    private FeedResponse sticky;

    @Schema(description = "Last fetched timestamp (only on page 1, main feed)")
    @JsonProperty("last_fetched_timestamp")
    private Long lastFetchedTimestamp;

    @Schema(description = "Execution time in seconds")
    @JsonProperty("execution_time")
    private Double executionTime;
}

