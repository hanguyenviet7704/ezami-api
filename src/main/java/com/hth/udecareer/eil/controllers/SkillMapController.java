package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.model.dto.SkillDto;
import com.hth.udecareer.eil.model.dto.WeakSkillDto;
import com.hth.udecareer.eil.model.response.SkillMapResponse;
import com.hth.udecareer.eil.service.MasteryService;
import com.hth.udecareer.eil.service.SkillService;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/users")
@Tag(name = "EIL Skill Map", description = "User skill map and mastery level APIs")
public class SkillMapController {

    private final MasteryService masteryService;
    private final SkillService skillService;
    private final UserRepository userRepository;

    @GetMapping("/me/skill-map")
    @Operation(summary = "Get my skill map", description = "Get the authenticated user's complete skill map")
    public ResponseEntity<SkillMapResponse> getMySkillMap(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting skill map for user {}", userId);
        SkillMapResponse response = masteryService.buildSkillMap(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/skill-map")
    @Operation(summary = "Get user skill map", description = "Get a user's complete skill map (admin only)")
    public ResponseEntity<SkillMapResponse> getUserSkillMap(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting skill map for user {}", userId);
        SkillMapResponse response = masteryService.buildSkillMap(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/weak-skills")
    @Operation(summary = "Get my weak skills", description = "Get the authenticated user's weak skills that need improvement")
    public ResponseEntity<List<WeakSkillDto>> getMyWeakSkills(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Maximum number of skills to return") @RequestParam(defaultValue = "10") int limit) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.debug("Getting weak skills for user {}", userId);
        List<WeakSkillDto> response = masteryService.getWeakSkills(userId, limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/initialize")
    @Operation(summary = "Initialize skill masteries", description = "Initialize mastery records for all skills for the authenticated user")
    public ResponseEntity<Map<String, String>> initializeMasteries(
            @Parameter(hidden = true) Principal principal) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        log.info("Initializing skill masteries for user {}", userId);
        masteryService.initializeAllMasteries(userId);
        return ResponseEntity.ok(Map.of("message", "Skill masteries initialized successfully"));
    }

    @GetMapping("/skills")
    @Operation(summary = "Get all skills", description = "Get all available skills in the system")
    public ResponseEntity<List<SkillDto>> getAllSkills() {
        List<SkillDto> skills = skillService.getAllActiveSkills().stream()
                .map(skillService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/skills/categories")
    @Operation(summary = "Get skill categories", description = "Get all skill categories")
    public ResponseEntity<List<String>> getSkillCategories() {
        List<String> categories = skillService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/skills/{skillId}")
    @Operation(summary = "Get skill by ID", description = "Get a specific skill by ID")
    public ResponseEntity<SkillDto> getSkillById(
            @Parameter(description = "Skill ID") @PathVariable Long skillId) throws AppException {

        SkillDto skill = skillService.toDto(skillService.getSkillById(skillId));
        return ResponseEntity.ok(skill);
    }

    @GetMapping("/skills/category/{categoryCode}")
    @Operation(summary = "Get skills by category code",
               description = "Get skills by category code (supports category names, certification IDs, and career paths)")
    public ResponseEntity<Map<String, Object>> getSkillsByCategoryCode(
            @Parameter(description = "Category code, certification ID, or career path (e.g., 'career-scrum_master', 'PSM_I')")
            @PathVariable String categoryCode) {

        log.debug("Getting skills by category code: {}", categoryCode);

        // Parse category code - handle career paths
        String normalizedCode = categoryCode.replace("career-", "").toUpperCase();

        List<SkillDto> skills = skillService.getSkillsByCategory(normalizedCode).stream()
                .map(skillService::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "skills", skills,
            "skillCount", skills.size(),
            "categoryCode", categoryCode
        ));
    }

    // ============= HELPER METHODS =============

    private Long getUserIdFromPrincipal(Principal principal) throws AppException {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        return user.getId();
    }
}
