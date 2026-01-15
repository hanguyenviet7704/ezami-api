package com.hth.udecareer.eil.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing user's diagnostic test history")
public class DiagnosticHistoryResponse {

    @Schema(description = "List of diagnostic attempts")
    private List<DiagnosticHistoryItem> items;

    @Schema(description = "Total number of completed diagnostics", example = "5")
    private Long totalCount;

    @Schema(description = "Current page number", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "10")
    private Integer size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Summary of a diagnostic attempt")
    public static class DiagnosticHistoryItem {

        @Schema(description = "Session ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String sessionId;

        @Schema(description = "Test type", example = "TOEIC")
        private String testType;

        @Schema(description = "Status", example = "COMPLETED")
        private String status;

        @Schema(description = "Total questions", example = "30")
        private Integer totalQuestions;

        @Schema(description = "Questions answered", example = "30")
        private Integer answeredQuestions;

        @Schema(description = "Raw score percentage (0-100)", example = "65.0")
        private BigDecimal rawScore;

        @Schema(description = "Estimated proficiency level", example = "INTERMEDIATE")
        private String estimatedLevel;

        @Schema(description = "Estimated minimum score", example = "450")
        private Integer estimatedScoreMin;

        @Schema(description = "Estimated maximum score", example = "550")
        private Integer estimatedScoreMax;

        @Schema(description = "Time spent in seconds", example = "1800")
        private Integer timeSpentSeconds;

        @Schema(description = "When diagnostic started")
        private LocalDateTime startTime;

        @Schema(description = "When diagnostic ended")
        private LocalDateTime endTime;

        @Schema(description = "When record was created")
        private LocalDateTime createdAt;
    }
}
