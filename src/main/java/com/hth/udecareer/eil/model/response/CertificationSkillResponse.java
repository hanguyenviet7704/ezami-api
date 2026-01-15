package com.hth.udecareer.eil.model.response;

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
@Schema(description = "Skill information with hierarchical structure")
public class CertificationSkillResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Skill ID", example = "1")
    private Long id;

    @Schema(description = "Parent skill ID", example = "null")
    private Long parentId;

    @Schema(description = "Certification ID", example = "PSM_I")
    private String certificationId;

    @Schema(description = "Skill code", example = "PSM_SCRUM_THEORY")
    private String code;

    @Schema(description = "Skill name", example = "Scrum Theory")
    private String name;

    @Schema(description = "Skill description")
    private String description;

    @Schema(description = "Hierarchy level (0=root)", example = "0")
    private Integer level;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;

    @Schema(description = "Number of mapped questions", example = "25")
    private Integer questionCount;

    @Schema(description = "Child skills (nested hierarchy)")
    private List<CertificationSkillResponse> children;
}
