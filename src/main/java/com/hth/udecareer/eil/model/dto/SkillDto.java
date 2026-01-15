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
@Schema(description = "Basic skill information")
public class SkillDto {

    @Schema(description = "Skill ID", example = "15")
    private Long id;

    @Schema(description = "Skill code", example = "LC_P2_WH")
    private String code;

    @Schema(description = "Skill name (localized based on Accept-Language header)",
            example = "WH Questions")
    private String name;

    @Schema(description = "Category", example = "LISTENING")
    private String category;

    @Schema(description = "Subcategory", example = "PART2")
    private String subcategory;

    @Schema(description = "Hierarchy level (1=category, 2=subcategory, 3=skill)", example = "3")
    private Integer level;
}
