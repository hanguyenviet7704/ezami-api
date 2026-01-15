package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.entities.EilReadinessSnapshotEntity;
import com.hth.udecareer.eil.service.ReadinessService;
import com.hth.udecareer.model.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eil/readiness")
@Tag(name = "EIL Readiness", description = "Readiness snapshot APIs (sync between Web & App dashboards)")
public class ReadinessController {

    private final ReadinessService readinessService;

    @GetMapping("/me/latest")
    @Operation(summary = "Get my latest readiness snapshot")
    public ResponseEntity<EilReadinessSnapshotEntity> myLatest(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(required = false) String testType
    ) {
        return ResponseEntity.ok(readinessService.getMyLatest(principal, testType));
    }

    @GetMapping("/me/history")
    @Operation(summary = "Get my readiness history (paged)")
    public ResponseEntity<PageResponse<EilReadinessSnapshotEntity>> myHistory(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(required = false) String testType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(readinessService.getMyHistory(principal, testType, page, size));
    }

    @GetMapping("/score")
    @Operation(summary = "Get current readiness score (simplified)")
    public ResponseEntity<EilReadinessSnapshotEntity> getScore(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(required = false) String testType
    ) {
        return ResponseEntity.ok(readinessService.getMyLatest(principal, testType));
    }
}

