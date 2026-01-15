package com.hth.udecareer.eil.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's mastery level for a skill")
public class SkillMasteryResponse {

    @Schema(description = "Skill ID", example = "15")
    private Long skillId;

    @Schema(description = "Skill code", example = "LC_P2_WH")
    private String skillCode;

    @Schema(description = "Skill name", example = "WH Questions")
    private String skillName;

    @Schema(description = "Skill name in Vietnamese", example = "Câu hỏi WH")
    private String skillNameVi;

    @Schema(description = "Category", example = "LISTENING")
    private String category;

    @Schema(description = "Subcategory", example = "PART2")
    private String subcategory;

    @Schema(description = "Mastery level (0.0 - 1.0)", example = "0.65")
    private Double masteryLevel;

    @Schema(description = "Confidence in mastery estimate (0.0 - 1.0)", example = "0.8")
    private Double confidence;

    @Schema(description = "Total attempts", example = "25")
    private Integer attempts;

    @Schema(description = "Correct answers count", example = "18")
    private Integer correctCount;

    @Schema(description = "Current correct streak", example = "3")
    private Integer streak;

    @Schema(description = "Mastery label", example = "PROFICIENT")
    private String masteryLabel;

    @Schema(description = "Last practice time")
    private LocalDateTime lastPracticed;
}
