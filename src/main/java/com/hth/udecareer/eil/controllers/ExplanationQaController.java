package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.entities.WpEzExplanationQaEntity;
import com.hth.udecareer.eil.repository.WpEzExplanationQaRepository;
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

import java.util.List;
import java.util.Map;

/**
 * Controller for Explanation QA API.
 * Provides endpoints to access AI-generated explanations from WordPress wp_ez_explanation_qa table.
 * Used for quality assurance and review of AI explanations.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/explanation-qa")
@Tag(name = "Explanation QA", description = "APIs for AI explanation quality assurance (ezami-admin-tools)")
public class ExplanationQaController {

    private final WpEzExplanationQaRepository explanationQaRepository;

    @GetMapping
    @Operation(summary = "Get all explanations", description = "Get paginated list of all AI explanations")
    public ResponseEntity<Page<WpEzExplanationQaEntity>> getAllExplanations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting all explanations, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<WpEzExplanationQaEntity> explanations = explanationQaRepository.findAll(pageable);
        return ResponseEntity.ok(explanations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get explanation by ID", description = "Get a specific explanation by ID")
    public ResponseEntity<WpEzExplanationQaEntity> getExplanationById(
            @Parameter(description = "Explanation ID") @PathVariable Long id) {

        log.debug("Getting explanation by ID: {}", id);
        WpEzExplanationQaEntity explanation = explanationQaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(explanation);
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "Get explanation for question", description = "Get AI explanation for a specific question")
    public ResponseEntity<WpEzExplanationQaEntity> getExplanationByQuestion(
            @Parameter(description = "Question ID") @PathVariable Long questionId) {

        log.debug("Getting explanation for question: {}", questionId);
        WpEzExplanationQaEntity explanation = explanationQaRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(explanation);
    }

    @GetMapping("/questions")
    @Operation(summary = "Get explanations for multiple questions", description = "Get AI explanations for a list of question IDs")
    public ResponseEntity<List<WpEzExplanationQaEntity>> getExplanationsByQuestions(
            @Parameter(description = "List of question IDs") @RequestParam List<Long> questionIds) {

        log.debug("Getting explanations for {} questions", questionIds.size());
        List<WpEzExplanationQaEntity> explanations = explanationQaRepository.findByQuestionIdIn(questionIds);
        return ResponseEntity.ok(explanations);
    }

    @GetMapping("/rating/{rating}")
    @Operation(summary = "Get explanations by rating", description = "Get explanations filtered by rating (good, bad, neutral)")
    public ResponseEntity<Page<WpEzExplanationQaEntity>> getExplanationsByRating(
            @Parameter(description = "Rating: good, bad, or neutral") @PathVariable String rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting explanations with rating: {}", rating);
        Pageable pageable = PageRequest.of(page, size);
        Page<WpEzExplanationQaEntity> explanations = explanationQaRepository.findByRatingOrderByCreatedAtDesc(rating, pageable);
        return ResponseEntity.ok(explanations);
    }

    @GetMapping("/unreviewed")
    @Operation(summary = "Get unreviewed explanations", description = "Get explanations that haven't been reviewed yet")
    public ResponseEntity<Page<WpEzExplanationQaEntity>> getUnreviewedExplanations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting unreviewed explanations");
        Pageable pageable = PageRequest.of(page, size);
        Page<WpEzExplanationQaEntity> explanations = explanationQaRepository.findUnreviewed(pageable);
        return ResponseEntity.ok(explanations);
    }

    @GetMapping("/prompt-version/{promptVersion}")
    @Operation(summary = "Get explanations by prompt version", description = "Get explanations generated with a specific prompt version")
    public ResponseEntity<Page<WpEzExplanationQaEntity>> getExplanationsByPromptVersion(
            @Parameter(description = "Prompt version") @PathVariable String promptVersion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting explanations for prompt version: {}", promptVersion);
        Pageable pageable = PageRequest.of(page, size);
        Page<WpEzExplanationQaEntity> explanations = explanationQaRepository.findByPromptVersionOrderByCreatedAtDesc(promptVersion, pageable);
        return ResponseEntity.ok(explanations);
    }

    @GetMapping("/search")
    @Operation(summary = "Search explanations", description = "Search explanations by question text keyword")
    public ResponseEntity<Page<WpEzExplanationQaEntity>> searchExplanations(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Searching explanations with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<WpEzExplanationQaEntity> explanations = explanationQaRepository.searchByQuestionText(keyword, pageable);
        return ResponseEntity.ok(explanations);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get explanation statistics", description = "Get overall statistics for AI explanations")
    public ResponseEntity<Map<String, Object>> getExplanationStats() {

        log.debug("Getting explanation statistics");
        long total = explanationQaRepository.count();
        long unreviewed = explanationQaRepository.countUnreviewed();
        long good = explanationQaRepository.countByRating("good");
        long bad = explanationQaRepository.countByRating("bad");
        long neutral = explanationQaRepository.countByRating("neutral");

        return ResponseEntity.ok(Map.of(
                "total", total,
                "unreviewed", unreviewed,
                "reviewed", total - unreviewed,
                "ratingBreakdown", Map.of(
                        "good", good,
                        "bad", bad,
                        "neutral", neutral
                )
        ));
    }

    @GetMapping("/stats/by-prompt")
    @Operation(summary = "Get stats by prompt version", description = "Get rating statistics grouped by prompt version")
    public ResponseEntity<List<Object[]>> getStatsByPromptVersion() {

        log.debug("Getting stats by prompt version");
        List<Object[]> stats = explanationQaRepository.getStatsByPromptVersion();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/performance")
    @Operation(summary = "Get performance stats by model", description = "Get latency and token usage statistics by AI model")
    public ResponseEntity<List<Object[]>> getPerformanceStats() {

        log.debug("Getting performance stats by model");
        List<Object[]> stats = explanationQaRepository.getPerformanceStatsByModel();
        return ResponseEntity.ok(stats);
    }
}
