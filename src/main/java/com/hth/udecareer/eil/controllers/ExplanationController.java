package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.entities.EilExplanationEntity;
import com.hth.udecareer.eil.service.ExplanationService;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/explanations")
@Tag(name = "EIL Explanations", description = "Cached question explanations APIs (eil_explanations)")
@SecurityRequirement(name = "bearerAuth")
public class ExplanationController {

    private final ExplanationService explanationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get explanation by ID")
    public ResponseEntity<EilExplanationEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                explanationService.getById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.EIL_EXPLANATION_NOT_FOUND))
        );
    }

    @GetMapping("/cache/{cacheKey}")
    @Operation(summary = "Get explanation by cache key (increments hit count)")
    public ResponseEntity<EilExplanationEntity> getByCacheKey(@PathVariable String cacheKey) {
        return ResponseEntity.ok(
                explanationService.getByCacheKey(cacheKey)
                        .orElseThrow(() -> new AppException(ErrorCode.EIL_EXPLANATION_NOT_FOUND))
        );
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "Get all explanations for a question")
    public ResponseEntity<List<EilExplanationEntity>> getByQuestionId(
            @PathVariable Long questionId,
            @RequestParam(required = false) String language
    ) {
        if (language != null) {
            return ResponseEntity.ok(explanationService.getByQuestionIdAndLanguage(questionId, language));
        }
        return ResponseEntity.ok(explanationService.getByQuestionId(questionId));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most accessed explanations")
    public ResponseEntity<List<EilExplanationEntity>> getMostAccessed(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(explanationService.getMostAccessed(limit));
    }
}
