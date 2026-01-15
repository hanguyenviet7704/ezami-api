package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Topic response")
public class TopicResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Topic ID", example = "1")
    private Long id;

    @Schema(description = "Topic title", example = "General Discussion")
    private String title;

    @Schema(description = "Topic slug", example = "general-discussion")
    private String slug;

    @Schema(description = "Topic description", example = "A place for general discussions")
    private String description;

    @Schema(description = "Number of feeds with this topic", example = "42")
    @JsonProperty("feeds_count")
    private Long feedsCount;

    @Schema(description = "Created at timestamp")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at timestamp")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
