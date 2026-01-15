package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Report statistics response")
public class ReportStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Total number of reports", example = "150")
    @JsonProperty("total_reports")
    private Long totalReports;

    @Schema(description = "Number of pending reports", example = "25")
    @JsonProperty("pending_reports")
    private Long pendingReports;

    @Schema(description = "Number of reports under review", example = "10")
    @JsonProperty("reviewing_reports")
    private Long reviewingReports;

    @Schema(description = "Number of resolved reports", example = "100")
    @JsonProperty("resolved_reports")
    private Long resolvedReports;

    @Schema(description = "Number of dismissed reports", example = "15")
    @JsonProperty("dismissed_reports")
    private Long dismissedReports;
}
