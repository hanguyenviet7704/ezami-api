package com.hth.udecareer.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.CheckUpdateVersionRequest;
import com.hth.udecareer.model.response.CheckUpdateVersionResponse;
import com.hth.udecareer.service.VersionService;

import lombok.RequiredArgsConstructor;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "App Versioning")
public class VersionController {

    private final VersionService versionService;

    @Operation(summary = "Check for app update", description = "Checks if a new version is available based on the client's current version and platform.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Update check successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CheckUpdateVersionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid platform or version format",
                    content = @Content
            ),
            @ApiResponse(responseCode = "default",
                    description = "Unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @PostMapping("/version/check-update")
    public CheckUpdateVersionResponse checkUpdate(@RequestBody CheckUpdateVersionRequest request)
            throws AppException {
        return versionService.checkUpdate(request);
    }
}