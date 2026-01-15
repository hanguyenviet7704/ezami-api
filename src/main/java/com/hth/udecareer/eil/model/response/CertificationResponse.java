package com.hth.udecareer.eil.model.response;

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
@Schema(description = "Certification information response")
public class CertificationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Certification ID", example = "PSM_I")
    private String certificationId;

    @Schema(description = "Certification name (English)", example = "Professional Scrum Master I")
    private String name;

    @Schema(description = "Certification name (Vietnamese)", example = "Chứng Chỉ Scrum Master Chuyên Nghiệp Cấp I")
    private String nameVi;

    @Schema(description = "Certification description")
    private String description;

    @Schema(description = "Primary category", example = "AGILE_METHODOLOGIES")
    private String primaryCategory;

    @Schema(description = "Secondary category (optional)", example = "QUALITY_ASSURANCE")
    private String secondaryCategory;

    @Schema(description = "Certification level", example = "ENTRY", allowableValues = {"ENTRY", "ASSOCIATE", "PROFESSIONAL", "EXPERT"})
    private String level;

    @Schema(description = "Issuing vendor/organization", example = "Scrum.org")
    private String vendor;

    @Schema(description = "Official exam code", example = "SAA-C03")
    private String examCode;

    @Schema(description = "Total number of skills", example = "17")
    private Integer skillCount;

    @Schema(description = "Total number of mapped questions", example = "150")
    private Integer questionCount;

    @Schema(description = "Is featured on homepage", example = "false")
    private Boolean isFeatured;
}
