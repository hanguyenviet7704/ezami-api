package com.hth.udecareer.eil.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Career domain response DTO.
 * Returns localized career domain information based on Accept-Language header.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Career domain information with associated certifications")
public class CareerDomainResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Career domain code", example = "SCRUM_MASTER")
    private String code;

    @Schema(description = "Career domain name (localized based on Accept-Language header)",
            example = "Scrum Master")
    private String name;

    @Schema(description = "Career domain description (localized based on Accept-Language header)",
            example = "Lead Scrum teams, facilitate agile ceremonies, and remove impediments")
    private String description;

    @Schema(description = "List of certification codes associated with this career domain",
            example = "[\"PSM_I\", \"SCRUM_PSM_II\"]")
    private List<String> certificationCodes;

    @Schema(description = "Number of certifications available for this career domain", example = "2")
    private Integer certificationCount;
}
