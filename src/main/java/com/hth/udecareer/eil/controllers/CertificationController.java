package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.response.CertificationResponse;
import com.hth.udecareer.eil.model.response.CertificationSkillResponse;
import com.hth.udecareer.eil.model.response.CertificationSkillTreeResponse;
import com.hth.udecareer.eil.service.CertificationSkillService;
import com.hth.udecareer.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Certification Skills API.
 * Provides endpoints to access skill taxonomy from WordPress wp_ez_skills table.
 * Used by both web and mobile apps for exam preparation features.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/certifications")
@Tag(name = "Certification Skills", description = "APIs for accessing certification skill taxonomy and question mappings")
public class CertificationController {

    private final CertificationSkillService certificationSkillService;

    // ============= CERTIFICATION ENDPOINTS =============

    @GetMapping
    @Operation(summary = "Get all certifications", description = "Get list of all available certifications with skill and question counts")
    public ResponseEntity<List<CertificationResponse>> getAllCertifications() {
        log.debug("Getting all certifications");
        List<CertificationResponse> certifications = certificationSkillService.getAllCertifications();
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/{certificationId}")
    @Operation(summary = "Get certification details", description = "Get details for a specific certification")
    public ResponseEntity<CertificationResponse> getCertification(
            @Parameter(description = "Certification ID", example = "PSM_I")
            @PathVariable String certificationId) throws AppException {

        log.debug("Getting certification: {}", certificationId);
        CertificationResponse certification = certificationSkillService.getCertification(certificationId);
        return ResponseEntity.ok(certification);
    }

    // ============= SKILL TREE ENDPOINTS =============

    @GetMapping("/{certificationId}/skills/tree")
    @Operation(summary = "Get skill tree", description = "Get complete hierarchical skill tree for a certification")
    public ResponseEntity<CertificationSkillTreeResponse> getSkillTree(
            @Parameter(description = "Certification ID", example = "PSM_I")
            @PathVariable String certificationId) throws AppException {

        log.debug("Getting skill tree for certification: {}", certificationId);
        CertificationSkillTreeResponse tree = certificationSkillService.getSkillTree(certificationId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/{certificationId}/skills")
    @Operation(summary = "Get skills list", description = "Get flat list of all skills for a certification")
    public ResponseEntity<List<CertificationSkillResponse>> getSkillsList(
            @Parameter(description = "Certification ID", example = "PSM_I")
            @PathVariable String certificationId) {

        log.debug("Getting skills list for certification: {}", certificationId);
        List<CertificationSkillResponse> skills = certificationSkillService.getSkillsList(certificationId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{certificationId}/skills/leaf")
    @Operation(summary = "Get leaf skills", description = "Get leaf skills (skills with no children) for a certification")
    public ResponseEntity<List<CertificationSkillResponse>> getLeafSkills(
            @Parameter(description = "Certification ID", example = "PSM_I")
            @PathVariable String certificationId) {

        log.debug("Getting leaf skills for certification: {}", certificationId);
        List<CertificationSkillResponse> skills = certificationSkillService.getLeafSkills(certificationId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{certificationId}/skills/level/{level}")
    @Operation(summary = "Get skills by level", description = "Get skills at a specific hierarchy level")
    public ResponseEntity<List<CertificationSkillResponse>> getSkillsByLevel(
            @Parameter(description = "Certification ID", example = "PSM_I")
            @PathVariable String certificationId,
            @Parameter(description = "Hierarchy level (0=root)", example = "1")
            @PathVariable Integer level) {

        log.debug("Getting skills at level {} for certification: {}", level, certificationId);
        List<CertificationSkillResponse> skills = certificationSkillService.getSkillsByLevel(certificationId, level);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{certificationId}/skills/search")
    @Operation(summary = "Search skills", description = "Search skills by keyword (name or code)")
    public ResponseEntity<List<CertificationSkillResponse>> searchSkills(
            @Parameter(description = "Certification ID", example = "PSM_I")
            @PathVariable String certificationId,
            @Parameter(description = "Search keyword", example = "scrum")
            @RequestParam String keyword) {

        log.debug("Searching skills with keyword '{}' in certification: {}", keyword, certificationId);
        List<CertificationSkillResponse> skills = certificationSkillService.searchSkills(certificationId, keyword);
        return ResponseEntity.ok(skills);
    }

    // ============= SKILL DETAIL ENDPOINTS =============

    @GetMapping("/skills/{skillId}")
    @Operation(summary = "Get skill by ID", description = "Get skill details by ID")
    public ResponseEntity<CertificationSkillResponse> getSkillById(
            @Parameter(description = "Skill ID", example = "1")
            @PathVariable Long skillId) throws AppException {

        log.debug("Getting skill by ID: {}", skillId);
        CertificationSkillResponse skill = certificationSkillService.getSkillById(skillId);
        return ResponseEntity.ok(skill);
    }

    @GetMapping("/skills/{skillId}/questions")
    @Operation(summary = "Get question IDs for skill", description = "Get list of question IDs mapped to a skill")
    public ResponseEntity<List<Long>> getQuestionIdsForSkill(
            @Parameter(description = "Skill ID", example = "1")
            @PathVariable Long skillId) {

        log.debug("Getting question IDs for skill: {}", skillId);
        List<Long> questionIds = certificationSkillService.getQuestionIdsForSkill(skillId);
        return ResponseEntity.ok(questionIds);
    }

    // ============= QUESTION MAPPING ENDPOINTS =============

    @GetMapping("/questions/{questionId}/skills")
    @Operation(summary = "Get skill IDs for question", description = "Get list of skill IDs mapped to a question")
    public ResponseEntity<List<Long>> getSkillIdsForQuestion(
            @Parameter(description = "Question ID", example = "123")
            @PathVariable Long questionId) {

        log.debug("Getting skill IDs for question: {}", questionId);
        List<Long> skillIds = certificationSkillService.getSkillIdsForQuestion(questionId);
        return ResponseEntity.ok(skillIds);
    }
}
