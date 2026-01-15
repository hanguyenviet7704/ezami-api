package com.hth.udecareer.eil.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to create or update an SRS card")
public class SrsCardRequest {

    @Schema(description = "Client-generated ID for sync", example = "client-123-abc")
    private String clientId;

    @NotNull(message = "Question ID is required")
    @Schema(description = "Question ID", example = "123")
    private Long questionId;

    @Schema(description = "Skill ID associated with the question")
    private Long skillId;

    @Schema(description = "Certification code", example = "PSM_I")
    private String certificationCode;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Request to review an SRS card")
    public static class ReviewRequest {
        @NotNull(message = "Quality rating is required")
        @Min(value = 0, message = "Quality must be at least 0")
        @Max(value = 5, message = "Quality must be at most 5")
        @Schema(description = "Quality of recall (0-5 SM-2 scale)", example = "4")
        private Integer quality;

        @Schema(description = "Time spent reviewing in seconds")
        private Integer timeSpentSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Request to create multiple SRS cards")
    public static class BulkCreateRequest {
        private List<SrsCardRequest> cards;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Request to sync SRS cards from client")
    public static class SyncRequest {
        @Schema(description = "Last sync timestamp")
        private Long lastSyncAt;

        @Schema(description = "Cards to sync from client")
        private List<SrsCardSyncData> cards;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SrsCardSyncData {
        private String clientId;
        private Long questionId;
        private Long skillId;
        private String certificationCode;
        private Double easeFactor;
        private Integer intervalDays;
        private Integer repetitions;
        private String status;
        private Integer totalReviews;
        private Integer correctReviews;
        private Integer lastQuality;
        private Long nextReviewAt;
        private Long lastReviewedAt;
        private Integer syncVersion;
    }
}
