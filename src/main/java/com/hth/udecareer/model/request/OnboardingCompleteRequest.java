package com.hth.udecareer.model.request;

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
@Schema(description = "Request to complete user onboarding")
public class OnboardingCompleteRequest {

    @Schema(description = "Selected career path (e.g., SCRUM_MASTER, DEVELOPER, PRODUCT_OWNER)", example = "SCRUM_MASTER")
    private String selectedCareerPath;

    @Schema(description = "List of selected certification codes", example = "[\"PSM_I\", \"PSPO_I\"]")
    private List<String> selectedCertifications;

    @Schema(description = "Target completion date (YYYY-MM-DD)", example = "2026-06-30")
    private String targetDate;

    @Schema(description = "User's current experience level", example = "BEGINNER")
    private String experienceLevel;

    @Schema(description = "Weekly study hours commitment", example = "10")
    private Integer weeklyStudyHours;
}
