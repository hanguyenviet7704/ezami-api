package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.service.TaxonomyOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller for taxonomy optimization and management
 *
 * IMPORTANT: All endpoints require ADMIN role
 */
@Slf4j
@ApiPrefixController
@RequestMapping("/admin/taxonomy")
@RequiredArgsConstructor
@Tag(name = "Taxonomy Admin", description = "Admin APIs for taxonomy optimization")
public class TaxonomyAdminController {

    private final TaxonomyOptimizationService taxonomyService;

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get taxonomy statistics", description = "Returns count of terms by taxonomy type")
    public ResponseEntity<ApiResponse> getStatistics() {
        Map<String, Long> stats = taxonomyService.getTaxonomyStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/duplicates/analyze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Analyze duplicate categories", description = "Find all duplicate categories (dry run)")
    public ResponseEntity<ApiResponse> analyzeDuplicates() {
        Map<String, List<Long>> duplicates = taxonomyService.analyzeDuplicateCategories();
        Map<Long, List<Long>> polylangDuplicates = taxonomyService.detectPolylangDuplicates();

        Map<String, Object> result = new HashMap<>();
        result.put("duplicatesByName", duplicates);
        result.put("polylangDuplicates", polylangDuplicates);
        result.put("totalDuplicateGroups", duplicates.size());
        result.put("totalPolylangGroups", polylangDuplicates.size());

        int totalDuplicateTerms = duplicates.values().stream()
            .mapToInt(List::size)
            .sum() - duplicates.size(); // Subtract canonical terms
        result.put("totalDuplicateTerms", totalDuplicateTerms);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/hierarchy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get category hierarchy", description = "Returns parent-child category relationships")
    public ResponseEntity<ApiResponse> getHierarchy() {
        Map<String, List<String>> hierarchy = taxonomyService.getCategoryHierarchy();
        return ResponseEntity.ok(ApiResponse.success(hierarchy));
    }

    @PostMapping("/duplicates/dry-run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Dry run cleanup",
        description = "Simulate cleanup without actually deleting. Shows what would be removed."
    )
    public ResponseEntity<ApiResponse> dryRunCleanup() {
        int duplicateCount = taxonomyService.dryRunCleanup();

        Map<String, Object> result = new HashMap<>();
        result.put("wouldRemove", duplicateCount);
        result.put("message", "This is a dry run. No data was deleted.");
        result.put("warning", "To actually cleanup, run the SQL script: scripts/cleanup_duplicate_categories.sql");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/duplicates/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "DANGEROUS: Cleanup duplicates",
        description = "Actually delete duplicate categories. BACKUP DATABASE FIRST!"
    )
    public ResponseEntity<ApiResponse> cleanupDuplicates() {
        // For safety, this is disabled
        return ResponseEntity.badRequest().body(
            ApiResponse.fail(
                HttpStatus.BAD_REQUEST,
                "This endpoint is disabled for safety. " +
                "Please backup your database and run the SQL script manually: " +
                "scripts/cleanup_duplicate_categories.sql"
            )
        );
    }
}
