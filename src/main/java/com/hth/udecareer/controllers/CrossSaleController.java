package com.hth.udecareer.controllers;


import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.enums.EcosystemApp;
import com.hth.udecareer.model.request.AppLogRequest;
import com.hth.udecareer.model.response.AppLogResponse;
import com.hth.udecareer.model.response.EcosystemAppResponse;
import com.hth.udecareer.service.CrossSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Gross Sale")
public class CrossSaleController {

    private final CrossSaleService crossSaleService;



    @Operation(summary = "Get all ecosystem apps", description = "Retrieve the list of all ecosystem apps with details like name, description, logo URL, App Store and Google Play URLs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Apps retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EcosystemAppResponse.class))),
            @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content)
    })
    @GetMapping("/cross-sale/apps")
    public List<EcosystemAppResponse> getApps() {
        return crossSaleService.getApps();
    }

    @Operation(summary = "Save app usage log", description = "Record a user's app usage log including app code and device OS. Requires authentication (JWT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log saved successfully",
                    content = @Content(schema = @Schema(implementation = AppLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid app code or device OS", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/cross-sale/log")
    public AppLogResponse saveAppLog(@RequestBody AppLogRequest appLogRequest,
                                     Principal principal) {
        return crossSaleService.saveLog(appLogRequest, principal.getName());
    }
}
