package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.dto.SkillDto;
import com.hth.udecareer.eil.service.CertificationSkillService;
import com.hth.udecareer.eil.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Skill Taxonomy APIs.
 * Provides endpoints to access skill taxonomy by category, certification, or career path.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Skill Taxonomy", description = "APIs for accessing skill taxonomy by category/certification/career path")
public class SkillTaxonomyController {

    private final SkillService skillService;
    private final CertificationSkillService certificationSkillService;

    // Support both /api/eil/skill/taxonomy and /api/skill/taxonomy
    @GetMapping({"/api/eil/skill/taxonomy", "/api/skill/taxonomy"})
    @Operation(summary = "Get skill taxonomy",
               description = "Get skills filtered by category code, certification ID, or career path")
    public ResponseEntity<Map<String, Object>> getSkillTaxonomy(
            @Parameter(description = "Category code (e.g., 'career-scrum_master', 'PSM_I', 'LISTENING')")
            @RequestParam String categoryCode) {

        log.debug("Getting skill taxonomy for category code: {}", categoryCode);

        // Parse category code - handle career paths
        String normalizedCode = categoryCode.replace("career-", "").toUpperCase();

        // Try to get skills by category first
        List<SkillDto> skills = skillService.getSkillsByCategory(normalizedCode).stream()
                .map(skillService::toDto)
                .collect(Collectors.toList());

        // If no skills found and looks like a certification ID, try certification endpoint
        if (skills.isEmpty()) {
            try {
                var certSkills = certificationSkillService.getSkillsList(normalizedCode);
                skills = certSkills.stream()
                        .map(certSkill -> SkillDto.builder()
                                .id(certSkill.getId())
                                .code(certSkill.getCode())
                                .name(certSkill.getName()) // Already localized
                                .category(certSkill.getCertificationId())
                                .subcategory(certSkill.getParentId() != null ? certSkill.getParentId().toString() : null)
                                .level(certSkill.getLevel())
                                .build())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.debug("Not a valid certification ID: {}", normalizedCode);
            }
        }

        return ResponseEntity.ok(Map.of(
            "skills", skills,
            "skillCount", skills.size(),
            "categoryCode", categoryCode
        ));
    }
}
