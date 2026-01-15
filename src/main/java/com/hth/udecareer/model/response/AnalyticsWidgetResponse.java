package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Analytics widget response")
public class AnalyticsWidgetResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Widget data with metrics")
    private Map<String, AnalyticsMetric> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsMetric implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Total count", example = "1250")
        @JsonProperty("total_records")
        private Long totalRecords;

        @Schema(description = "Comparison with previous period", example = "+15%")
        private String comparison;

        @Schema(description = "Metric title", example = "Total Posts")
        private String title;
    }
}
