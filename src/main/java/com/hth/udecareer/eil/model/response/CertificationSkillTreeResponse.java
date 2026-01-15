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
@Schema(description = "Complete skill tree for a certification")
public class CertificationSkillTreeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Certification ID", example = "PSM_I")
    private String certificationId;

    @Schema(description = "Certification name", example = "Professional Scrum Master I")
    private String certificationName;

    @Schema(description = "Total skills count", example = "17")
    private Integer totalSkills;

    @Schema(description = "Total questions mapped", example = "150")
    private Integer totalQuestions;

    @Schema(description = "Root skills with nested children")
    private List<CertificationSkillResponse> skills;
}
