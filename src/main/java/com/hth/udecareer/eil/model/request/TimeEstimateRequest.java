package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to create a time estimate for certification")
public class TimeEstimateRequest {

    @NotBlank(message = "Certification code is required")
    @Schema(description = "Certification code", example = "PSM_I")
    private String certificationCode;

    @Schema(description = "Certification name", example = "Professional Scrum Master I")
    private String certificationName;

    @Schema(description = "Target mastery level (0-1)", example = "0.8")
    private BigDecimal targetMastery;

    @Schema(description = "Target date to achieve certification")
    private LocalDate targetDate;

    @Schema(description = "Recommended daily study hours", example = "2.0")
    private BigDecimal recommendedDailyHours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Request to update progress on time estimate")
    public static class ProgressUpdateRequest {

        @Schema(description = "Study hours to add", example = "1.5")
        private BigDecimal studyHours;

        @Schema(description = "Questions practiced in this session")
        private Integer questionsPracticed;

        @Schema(description = "New mastery level if known")
        private BigDecimal newMastery;

        @Schema(description = "Session completed flag")
        @Builder.Default
        private Boolean sessionCompleted = true;
    }
}
