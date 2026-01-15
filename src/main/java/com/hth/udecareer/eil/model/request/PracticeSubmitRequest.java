package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to submit an answer in practice session")
public class PracticeSubmitRequest {

    @NotBlank(message = "Session ID is required")
    @Schema(description = "Practice session ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @NotNull(message = "Question ID is required")
    @Schema(description = "Question ID", example = "123")
    private Long questionId;

    @NotNull(message = "Answer data is required")
    @Schema(description = "Answer data as array of booleans", example = "[false, true, false, false]")
    private List<Boolean> answerData;

    @Schema(description = "Time spent on this question in seconds", example = "30")
    private Integer timeSpentSeconds;

    @Schema(description = "Request AI explanation with submission", example = "true", defaultValue = "false")
    @Builder.Default
    private Boolean requestExplanation = false;
}
