package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Analytics activity chart response")
public class AnalyticsActivityResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Activity type", example = "posts")
    private String activity;

    @Schema(description = "Chart title", example = "Posts Activity")
    private String title;

    @Schema(description = "Activity data points")
    private List<ActivityDataPoint> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDataPoint implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Date", example = "2025-01-15")
        private String date;

        @Schema(description = "Value", example = "42")
        private Long value;
    }
}
