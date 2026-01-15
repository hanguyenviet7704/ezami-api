package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "Request to create a pretty (short) affiliate link")
public class CreatePrettyLinkRequest {

    @NotNull(message = "Affiliate ID is required")
    @Schema(description = "The ID of the affiliate", example = "123")
    private Long affiliateId;

    @NotBlank(message = "Target URL is required")
    @Schema(description = "The target URL that the short link will redirect to",
            example = "https://udecareer.com/courses/java-fundamentals")
    private String targetUrl;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$",
             message = "Slug can only contain alphanumeric characters and dashes")
    @Schema(description = "The desired slug for the short link (only alphanumeric and dashes allowed)",
            example = "java-course-2024")
    private String desiredSlug;
}

