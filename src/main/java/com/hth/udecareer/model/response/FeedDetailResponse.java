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
@Schema(description = "Feed detail response")
public class FeedDetailResponse {

    @Schema(description = "Feed information")
    private FeedResponse feed;

    @Schema(description = "Execution time in seconds")
    @JsonProperty("execution_time")
    private Double executionTime;
}

