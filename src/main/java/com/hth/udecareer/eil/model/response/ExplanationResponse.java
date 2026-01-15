package com.hth.udecareer.eil.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI-generated explanation for a question")
public class ExplanationResponse {

    @Schema(description = "Question ID", example = "123")
    private Long questionId;

    @Schema(description = "Whether user's answer was correct", example = "false")
    private Boolean isCorrect;

    @Schema(description = "Correct answer", example = "[false, true, false, false]")
    private List<Boolean> correctAnswer;

    // Structured explanation
    @Schema(description = "Brief summary of the explanation")
    private String summary;

    @Schema(description = "Why the correct answer is right")
    private String whyCorrect;

    @Schema(description = "Why user's answer was wrong (if incorrect)")
    private String whyWrong;

    @Schema(description = "Key learning points")
    private List<String> keyPoints;

    @Schema(description = "Relevant grammar rule (if applicable)")
    private String grammarRule;

    @Schema(description = "Vocabulary tip (if applicable)")
    private String vocabularyTip;

    @Schema(description = "Related examples")
    private List<String> relatedExamples;

    // Metadata
    @Schema(description = "Whether response came from cache", example = "true")
    private Boolean fromCache;

    @Schema(description = "Cache key for this explanation")
    private String cacheKey;

    @Schema(description = "Generation time in milliseconds", example = "1500")
    private Long generationTimeMs;
}
