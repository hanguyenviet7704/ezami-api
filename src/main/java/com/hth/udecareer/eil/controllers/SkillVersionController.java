package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.entities.WpEzSkillVersionEntity;
import com.hth.udecareer.eil.repository.WpEzSkillVersionRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Skill Version History API.
 * Provides endpoints to access skill version history from WordPress wp_ez_skills_versions table.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/skills")
@Tag(name = "Skill Versions", description = "APIs for accessing skill version history (ezami-admin-tools)")
public class SkillVersionController {

    private final WpEzSkillVersionRepository skillVersionRepository;

    @GetMapping("/{skillId}/versions")
    @Operation(summary = "Get skill version history", description = "Get all versions for a specific skill")
    public ResponseEntity<Page<WpEzSkillVersionEntity>> getSkillVersions(
            @Parameter(description = "Skill ID") @PathVariable Long skillId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting version history for skill: {}", skillId);
        Pageable pageable = PageRequest.of(page, size);
        Page<WpEzSkillVersionEntity> versions = skillVersionRepository.findBySkillIdOrderByVersionDesc(skillId, pageable);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{skillId}/versions/{version}")
    @Operation(summary = "Get specific skill version", description = "Get a specific version of a skill")
    public ResponseEntity<WpEzSkillVersionEntity> getSkillVersion(
            @Parameter(description = "Skill ID") @PathVariable Long skillId,
            @Parameter(description = "Version number") @PathVariable Integer version) {

        log.debug("Getting version {} for skill: {}", version, skillId);
        WpEzSkillVersionEntity skillVersion = skillVersionRepository.findBySkillIdAndVersion(skillId, version)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(skillVersion);
    }

    @GetMapping("/{skillId}/versions/latest")
    @Operation(summary = "Get latest skill version", description = "Get the most recent version of a skill")
    public ResponseEntity<WpEzSkillVersionEntity> getLatestSkillVersion(
            @Parameter(description = "Skill ID") @PathVariable Long skillId) {

        log.debug("Getting latest version for skill: {}", skillId);
        WpEzSkillVersionEntity latestVersion = skillVersionRepository.findLatestBySkillId(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(latestVersion);
    }

    @GetMapping("/{skillId}/versions/count")
    @Operation(summary = "Get skill version count", description = "Get total number of versions for a skill")
    public ResponseEntity<Map<String, Object>> getSkillVersionCount(
            @Parameter(description = "Skill ID") @PathVariable Long skillId) {

        log.debug("Getting version count for skill: {}", skillId);
        long count = skillVersionRepository.countBySkillId(skillId);
        return ResponseEntity.ok(Map.of(
                "skillId", skillId,
                "versionCount", count
        ));
    }
}
