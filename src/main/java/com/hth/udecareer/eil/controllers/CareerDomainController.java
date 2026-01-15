package com.hth.udecareer.eil.controllers;

import com.hth.udecareer.eil.enums.CareerDomain;
import com.hth.udecareer.eil.model.response.CareerDomainResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Career Domains API.
 * Provides endpoints to access career domain information with localized content.
 */
@Slf4j
@RestController
@RequestMapping("/api/career-domains")
@Tag(name = "Career Domains", description = "APIs for accessing career domain information and associated certifications")
public class CareerDomainController {

    @GetMapping
    @Operation(
            summary = "Get all career domains",
            description = "Get list of all career domains with localized names and descriptions. " +
                    "Content is localized based on Accept-Language header (en/vi)"
    )
    public ResponseEntity<List<CareerDomainResponse>> getAllCareerDomains() {
        log.debug("Getting all career domains");

        List<CareerDomainResponse> careerDomains = Arrays.stream(CareerDomain.values())
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(careerDomains);
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Get career domain by code",
            description = "Get details for a specific career domain by its code"
    )
    public ResponseEntity<CareerDomainResponse> getCareerDomainByCode(
            @Parameter(description = "Career domain code", example = "SCRUM_MASTER")
            @PathVariable String code) {

        log.debug("Getting career domain: {}", code);

        CareerDomain domain = CareerDomain.fromCode(code);
        if (domain == null) {
            log.warn("Career domain not found: {}", code);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(domain));
    }

    /**
     * Convert CareerDomain enum to CareerDomainResponse DTO.
     */
    private CareerDomainResponse toResponse(CareerDomain domain) {
        return CareerDomainResponse.builder()
                .code(domain.getCode())
                .name(domain.getLocalizedName())
                .description(domain.getLocalizedDescription())
                .certificationCodes(domain.getCertificationCodes())
                .certificationCount(domain.getCertificationCodes() != null ?
                        domain.getCertificationCodes().size() : 0)
                .build();
    }
}
