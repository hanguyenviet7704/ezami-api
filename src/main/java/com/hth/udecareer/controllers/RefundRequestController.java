package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.RefundRequestEntity;
import com.hth.udecareer.model.request.RefundRequestCreateRequest;
import com.hth.udecareer.model.request.RefundRequestDecisionRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.service.RefundRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Refund Requests", description = "Refund request APIs for App (create/view) and Web (admin decision)")
@SecurityRequirement(name = "bearerAuth")
public class RefundRequestController {

    private final RefundRequestService refundRequestService;

    @PostMapping("/refund-requests")
    @Operation(summary = "Create my refund request")
    public ResponseEntity<RefundRequestEntity> create(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody RefundRequestCreateRequest body
    ) {
        return ResponseEntity.ok(refundRequestService.createMyRequest(principal, body));
    }

    @GetMapping("/refund-requests/my")
    @Operation(summary = "List my refund requests")
    public ResponseEntity<PageResponse<RefundRequestEntity>> my(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(refundRequestService.getMyRequests(principal, page, size));
    }

    @GetMapping("/admin/refund-requests")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "Admin: list refund requests")
    public ResponseEntity<PageResponse<RefundRequestEntity>> adminList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(refundRequestService.getAllRequests(page, size));
    }

    @PostMapping("/admin/refund-requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "Admin: approve refund request")
    public ResponseEntity<RefundRequestEntity> approve(
            @PathVariable("id") Long id,
            @RequestBody(required = false) RefundRequestDecisionRequest body
    ) {
        return ResponseEntity.ok(refundRequestService.approve(id, body));
    }

    @PostMapping("/admin/refund-requests/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "Admin: reject refund request")
    public ResponseEntity<RefundRequestEntity> reject(
            @PathVariable("id") Long id,
            @RequestBody(required = false) RefundRequestDecisionRequest body
    ) {
        return ResponseEntity.ok(refundRequestService.reject(id, body));
    }
}

