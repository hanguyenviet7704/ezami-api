package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.entities.WpEzExplanationPromptEntity;
import com.hth.udecareer.eil.repository.WpEzExplanationPromptRepository;
import com.hth.udecareer.enums.ErrorCode;
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
 * Controller for Explanation Prompt API.
 * Provides endpoints to access AI prompt templates from WordPress wp_ez_explanation_prompts table.
 * Used for managing prompt versions for AI explanation generation.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/explanation-prompts")
@Tag(name = "Explanation Prompts", description = "APIs for AI prompt template management (ezami-admin-tools)")
public class ExplanationPromptController {

    private final WpEzExplanationPromptRepository promptRepository;

    @GetMapping
    @Operation(summary = "Get all prompts", description = "Get all prompt templates ordered by creation date")
    public ResponseEntity<List<WpEzExplanationPromptEntity>> getAllPrompts() {

        log.debug("Getting all explanation prompts");
        List<WpEzExplanationPromptEntity> prompts = promptRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prompt by ID", description = "Get a specific prompt template by ID")
    public ResponseEntity<WpEzExplanationPromptEntity> getPromptById(
            @Parameter(description = "Prompt ID") @PathVariable Long id) {

        log.debug("Getting prompt by ID: {}", id);
        WpEzExplanationPromptEntity prompt = promptRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(prompt);
    }

    @GetMapping("/version/{version}")
    @Operation(summary = "Get prompt by version", description = "Get a specific prompt template by version string")
    public ResponseEntity<WpEzExplanationPromptEntity> getPromptByVersion(
            @Parameter(description = "Prompt version", example = "v1.0") @PathVariable String version) {

        log.debug("Getting prompt by version: {}", version);
        WpEzExplanationPromptEntity prompt = promptRepository.findByVersion(version)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(prompt);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active prompt", description = "Get the currently active prompt template")
    public ResponseEntity<WpEzExplanationPromptEntity> getActivePrompt() {

        log.debug("Getting active prompt");
        WpEzExplanationPromptEntity prompt = promptRepository.findActivePrompt()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(prompt);
    }

    @GetMapping("/model/{model}")
    @Operation(summary = "Get prompts by model", description = "Get prompt templates for a specific AI model")
    public ResponseEntity<List<WpEzExplanationPromptEntity>> getPromptsByModel(
            @Parameter(description = "AI model name", example = "gpt-4") @PathVariable String model) {

        log.debug("Getting prompts for model: {}", model);
        List<WpEzExplanationPromptEntity> prompts = promptRepository.findByModelOrderByCreatedAtDesc(model);
        return ResponseEntity.ok(prompts);
    }
}
