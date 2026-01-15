package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing the created pretty link")
public class PrettyLinkResponse {

    @Schema(description = "The full short URL",
            example = "https://udecareer.com/go/java-course-2024")
    private String prettyUrl;

    @Schema(description = "The slug part of the URL",
            example = "java-course-2024")
    private String slug;

    @Schema(description = "The affiliate URL with tracking parameter",
            example = "https://udecareer.com/courses/java-fundamentals?ref=123")
    private String affiliateUrl;

    @Schema(description = "The original target URL",
            example = "https://udecareer.com/courses/java-fundamentals")
    private String originalUrl;

    @Schema(description = "The affiliate ID", example = "123")
    private Long affiliateId;

    @Schema(description = "The affiliate link ID", example = "456")
    private Long affiliateLinkId;
}

