package com.hth.udecareer.service;

import com.hth.udecareer.entities.TermEntity;
import com.hth.udecareer.entities.TermTaxonomyEntity;
import com.hth.udecareer.repository.TermRepository;
import com.hth.udecareer.repository.TermTaxonomyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to optimize and manage WordPress taxonomy structure
 *
 * Handles:
 * - Detecting duplicate categories
 * - Cleaning up Polylang duplicates
 * - Consolidating taxonomy structure
 * - Managing term relationships
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaxonomyOptimizationService {

    private final TermRepository termRepository;
    private final TermTaxonomyRepository termTaxonomyRepository;

    /**
     * Analyze duplicate categories
     *
     * @return Map of category name -> list of duplicate term IDs
     */
    public Map<String, List<Long>> analyzeDuplicateCategories() {
        log.info("Analyzing duplicate categories...");

        List<TermTaxonomyEntity> categories = termTaxonomyRepository.findByTaxonomy("category");

        Map<String, List<Long>> duplicates = categories.stream()
            .filter(tt -> tt.getTerm() != null)
            .collect(Collectors.groupingBy(
                tt -> tt.getTerm().getName(),
                Collectors.mapping(tt -> tt.getTerm().getId(), Collectors.toList())
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("Found {} categories with duplicates", duplicates.size());
        duplicates.forEach((name, ids) ->
            log.info("  - '{}': {} duplicates (IDs: {})", name, ids.size(), ids)
        );

        return duplicates;
    }

    /**
     * Detect Polylang duplicate categories
     *
     * @return Map of canonical term ID -> list of duplicate term IDs
     */
    public Map<Long, List<Long>> detectPolylangDuplicates() {
        log.info("Detecting Polylang duplicate categories...");

        List<TermTaxonomyEntity> categories = termTaxonomyRepository.findByTaxonomy("category");

        // Group by name (without language suffix)
        Map<String, List<TermTaxonomyEntity>> grouped = categories.stream()
            .filter(tt -> tt.getTerm() != null)
            .collect(Collectors.groupingBy(tt -> tt.getTerm().getName()));

        Map<Long, List<Long>> polylangDuplicates = new HashMap<>();

        for (Map.Entry<String, List<TermTaxonomyEntity>> entry : grouped.entrySet()) {
            List<TermTaxonomyEntity> group = entry.getValue();
            if (group.size() <= 1) continue;

            // Find canonical (original) term - the one with smallest ID without -pll_ suffix
            Optional<TermTaxonomyEntity> canonical = group.stream()
                .filter(tt -> !tt.getTerm().getSlug().contains("-pll_"))
                .min(Comparator.comparing(tt -> tt.getTerm().getId()));

            if (!canonical.isPresent()) {
                // If all have -pll_ suffix, take the first one
                canonical = group.stream()
                    .min(Comparator.comparing(tt -> tt.getTerm().getId()));
            }

            if (canonical.isPresent()) {
                Long canonicalId = canonical.get().getTerm().getId();
                List<Long> duplicateIds = group.stream()
                    .map(tt -> tt.getTerm().getId())
                    .filter(id -> !id.equals(canonicalId))
                    .collect(Collectors.toList());

                if (!duplicateIds.isEmpty()) {
                    polylangDuplicates.put(canonicalId, duplicateIds);
                }
            }
        }

        log.info("Found {} Polylang duplicate groups", polylangDuplicates.size());
        return polylangDuplicates;
    }

    /**
     * Get taxonomy statistics
     *
     * @return Map of taxonomy type -> count
     */
    public Map<String, Long> getTaxonomyStatistics() {
        log.info("Getting taxonomy statistics...");

        List<TermTaxonomyEntity> allTaxonomies = termTaxonomyRepository.findAll();

        Map<String, Long> stats = allTaxonomies.stream()
            .collect(Collectors.groupingBy(
                TermTaxonomyEntity::getTaxonomy,
                Collectors.counting()
            ));

        stats.forEach((taxonomy, count) ->
            log.info("  - {}: {}", taxonomy, count)
        );

        return stats;
    }

    /**
     * Clean up duplicate categories (dry run)
     * Shows what would be deleted without actually deleting
     *
     * @return Number of duplicates that would be removed
     */
    public int dryRunCleanup() {
        log.info("=== DRY RUN: Cleanup Duplicate Categories ===");

        Map<Long, List<Long>> duplicates = detectPolylangDuplicates();

        int totalDuplicates = duplicates.values().stream()
            .mapToInt(List::size)
            .sum();

        log.info("Would remove {} duplicate categories", totalDuplicates);
        log.info("Would keep {} canonical categories", duplicates.size());

        duplicates.forEach((canonicalId, duplicateIds) -> {
            Optional<TermEntity> canonical = termRepository.findById(canonicalId);
            canonical.ifPresent(term ->
                log.info("  - Keep: {} (ID: {}), Remove: {} duplicates",
                    term.getName(), canonicalId, duplicateIds.size())
            );
        });

        return totalDuplicates;
    }

    /**
     * Get category hierarchy
     *
     * @return Map of parent category -> list of child categories
     */
    public Map<String, List<String>> getCategoryHierarchy() {
        log.info("Getting category hierarchy...");

        List<TermTaxonomyEntity> categories = termTaxonomyRepository.findByTaxonomy("category");

        Map<String, List<String>> hierarchy = new HashMap<>();

        for (TermTaxonomyEntity category : categories) {
            if (category.getTerm() == null) continue;

            String categoryName = category.getTerm().getName();

            if (category.getParent() != null && category.getParent() > 0) {
                // Find parent
                Optional<TermTaxonomyEntity> parent = termTaxonomyRepository.findById(category.getParent());
                if (parent.isPresent() && parent.get().getTerm() != null) {
                    String parentName = parent.get().getTerm().getName();
                    hierarchy.computeIfAbsent(parentName, k -> new ArrayList<>())
                        .add(categoryName);
                }
            } else {
                // Root category
                hierarchy.computeIfAbsent("ROOT", k -> new ArrayList<>())
                    .add(categoryName);
            }
        }

        return hierarchy;
    }

    /**
     * DANGEROUS: Actually clean up duplicate categories
     * USE WITH EXTREME CAUTION - BACKUP DATABASE FIRST!
     *
     * This method will:
     * 1. Merge duplicate term relationships to canonical term
     * 2. Delete duplicate taxonomy entries
     * 3. Delete duplicate terms
     *
     * @return Number of duplicates removed
     */
    @Transactional
    public int cleanupDuplicates() {
        log.warn("=== EXECUTING CLEANUP - THIS WILL DELETE DATA ===");

        // TODO: Implement actual cleanup logic
        // For safety, this should:
        // 1. Create backup
        // 2. Update term_relationships
        // 3. Delete duplicate term_taxonomy
        // 4. Delete duplicate terms
        // 5. Update term counts

        throw new UnsupportedOperationException(
            "For safety, this method is not implemented. " +
            "Please run the SQL script manually after backup."
        );
    }
}
