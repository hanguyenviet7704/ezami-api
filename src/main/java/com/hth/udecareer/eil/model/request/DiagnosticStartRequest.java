package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to start a diagnostic test")
public class DiagnosticStartRequest {

    @Schema(description = "Assessment mode: CAREER_ASSESSMENT (determine user level) or CERTIFICATION_PRACTICE (practice for specific cert)",
            example = "CAREER_ASSESSMENT", allowableValues = {"CAREER_ASSESSMENT", "CERTIFICATION_PRACTICE"})
    private String mode;

    @Schema(description = "Target test type (deprecated, use certificationCode instead)", example = "TOEIC", defaultValue = "TOEIC")
    @Deprecated
    private String testType;

    @Schema(description = "Certification code for CERTIFICATION_PRACTICE mode", example = "PSM_I")
    private String certificationCode;

    @Schema(description = "Career path for CAREER_ASSESSMENT mode (e.g., SCRUM_MASTER, PRODUCT_OWNER, DEVELOPER, QA_ENGINEER)",
            example = "SCRUM_MASTER")
    private String careerPath;

    @Min(value = 10, message = "Question count must be at least 10")
    @Max(value = 50, message = "Question count must not exceed 50")
    @Schema(description = "Number of questions (10-50)", example = "30", minimum = "10", maximum = "50")
    private Integer questionCount;

    @Schema(description = "Focus categories (optional). If empty, all categories included.",
            example = "[\"LISTENING\", \"READING\"]")
    private List<String> focusCategories;

    /**
     * Get effective mode, defaults to CAREER_ASSESSMENT if not specified
     */
    public String getEffectiveMode() {
        if (mode != null && !mode.isEmpty()) {
            return mode;
        }
        // If certificationCode is provided, assume CERTIFICATION_PRACTICE
        if (certificationCode != null && !certificationCode.isEmpty()) {
            return "CERTIFICATION_PRACTICE";
        }
        // Default to CAREER_ASSESSMENT
        return "CAREER_ASSESSMENT";
    }
}
