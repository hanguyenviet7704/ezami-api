package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to start a practice session")
public class PracticeStartRequest {

    @Schema(description = "Session type: ADAPTIVE, SKILL_FOCUS, REVIEW, MIXED",
            example = "ADAPTIVE", defaultValue = "ADAPTIVE")
    private String sessionType;

    @Schema(description = "Target skill ID (required for SKILL_FOCUS type)", example = "15")
    private Long targetSkillId;

    @Schema(description = "List of skill IDs to focus on (alternative to targetSkillId for multiple skills)",
            example = "[15, 20, 25]")
    private List<Long> focusSkills;

    @Schema(description = "Target categories to practice",
            example = "[\"LISTENING\"]")
    private List<String> targetCategories;

    @Schema(description = "Maximum questions per session (5-50)", example = "20",
            minimum = "5", maximum = "50")
    private Integer maxQuestions;
}
