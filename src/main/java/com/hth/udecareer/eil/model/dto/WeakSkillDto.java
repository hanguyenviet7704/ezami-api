package com.hth.udecareer.eil.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Information about a weak skill")
public class WeakSkillDto {

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

    @Schema(description = "Current mastery level (0.0 - 1.0)", example = "0.25")
    private Double masteryLevel;

    @Schema(description = "Mastery label", example = "WEAK")
    private String masteryLabel;

    @Schema(description = "Number of attempts", example = "5")
    private Integer attempts;

    @Schema(description = "Accuracy percentage (0-100)", example = "40.0")
    private Double accuracy;

    @Schema(description = "Priority rank for improvement (1 = highest priority)", example = "1")
    private Integer priorityRank;

    @Schema(description = "Recommended action", example = "Focus on Part 2 WH question patterns")
    private String recommendation;
}
