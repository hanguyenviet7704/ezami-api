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
@Schema(description = "Term/Topic information")
public class TermResponse {

    @Schema(description = "Term ID", example = "1")
    private Long id;

    @Schema(description = "Term title", example = "Tutorial")
    private String title;

    @Schema(description = "Term slug", example = "tutorial")
    private String slug;
}

