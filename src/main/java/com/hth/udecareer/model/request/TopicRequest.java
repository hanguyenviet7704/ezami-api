package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Topic create/update request")
public class TopicRequest {

    @Schema(description = "Topic ID (for update)", example = "1")
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Topic title", example = "General Discussion", required = true)
    private String title;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Schema(description = "Topic slug (auto-generated if not provided)", example = "general-discussion")
    private String slug;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Topic description", example = "A place for general discussions")
    private String description;
}
