package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.entities.EilAiFeedbackEntity;
import com.hth.udecareer.eil.service.AiFeedbackService;
import com.hth.udecareer.model.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/ai-feedback")
@Tag(name = "EIL AI Feedback", description = "AI-generated feedback APIs (eil_ai_feedback)")
@SecurityRequirement(name = "bearerAuth")
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    @GetMapping("/my")
    @Operation(summary = "Get my AI feedback history with pagination")
    public ResponseEntity<PageResponse<EilAiFeedbackEntity>> getMyFeedback(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(aiFeedbackService.getMyFeedback(principal, page, size));
    }

    @GetMapping("/my/type/{feedbackType}")
    @Operation(summary = "Get my AI feedback by type")
    public ResponseEntity<List<EilAiFeedbackEntity>> getMyFeedbackByType(
            @Parameter(hidden = true) Principal principal,
            @PathVariable String feedbackType
    ) {
        return ResponseEntity.ok(aiFeedbackService.getMyFeedbackByType(principal, feedbackType));
    }

    @GetMapping("/my/latest")
    @Operation(summary = "Get my latest AI feedback by type")
    public ResponseEntity<EilAiFeedbackEntity> getLatestByType(
            @Parameter(hidden = true) Principal principal,
            @RequestParam String feedbackType
    ) {
        return ResponseEntity.ok(aiFeedbackService.getLatestByType(principal, feedbackType));
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate an AI feedback")
    public ResponseEntity<EilAiFeedbackEntity> rateFeedback(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long id,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Boolean isHelpful
    ) {
        return ResponseEntity.ok(aiFeedbackService.rateFeedback(principal, id, rating, comment, isHelpful));
    }
}
