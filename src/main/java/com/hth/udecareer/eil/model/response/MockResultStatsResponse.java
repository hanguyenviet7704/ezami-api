package com.hth.udecareer.eil.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockResultStatsResponse {

    @JsonProperty("certificateCode")
    private String certificateCode;

    @JsonProperty("maxScore")
    private Double maxScore;

    @JsonProperty("avgScore")
    private Double avgScore;

    @JsonProperty("totalAttempts")
    private Long totalAttempts;

    @JsonProperty("passedAttempts")
    private Long passedAttempts;
}
