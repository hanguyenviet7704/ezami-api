package com.hth.udecareer.eil.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to analyze a completed mock test")
public class MockFinishRequest {

    @NotNull(message = "Quiz ID is required")
    @Schema(description = "Quiz ID from existing quiz system", example = "456")
    private Long quizId;

    @NotNull(message = "Activity ID is required")
    @Schema(description = "Activity ID from quiz submission", example = "789")
    private Long activityId;
}
