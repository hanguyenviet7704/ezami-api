package com.hth.udecareer.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Response nhận từ service AI phân tích readiness/weakness")
public class AIAnalyticsResponse {

    @Schema(description = "Dữ liệu phân tích")
    private DataSection data;

    @Schema(description = "Metadata đi kèm từ AI")
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSection {

        @JsonProperty("readiness_level")
        @Schema(description = "Mức độ sẵn sàng", example = "NOT_READY")
        private String readinessLevel;

        @JsonProperty("weak_areas")
        @Schema(description = "Danh sách khu vực/yếu điểm")
        private List<WeakArea> weakAreas;

        @Schema(description = "Khuyến nghị/hành động được đề xuất")
        private List<Recommendation> recommendations;

        @JsonProperty("exam_readiness")
        @Schema(description = "Thông tin sẵn sàng cho kỳ thi")
        private ExamReadiness examReadiness;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeakArea {

        @Schema(description = "Chương/section", example = "1: Fundamentals")
        private String chapter;

        @Schema(description = "Tên chủ đề", example = "Boundary value analysis")
        private String name;

        @JsonProperty("current_accuracy")
        @Schema(description = "Độ chính xác hiện tại", example = "55.0")
        private Double currentAccuracy;

        @JsonProperty("target_accuracy")
        @Schema(description = "Độ chính xác mục tiêu", example = "75.0")
        private Double targetAccuracy;

        @Schema(description = "Mức ưu tiên", example = "CRITICAL")
        private String priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {

        @Schema(description = "Tiêu đề khuyến nghị", example = "Ôn lại chương 2")
        private String title;

        @Schema(description = "Mô tả hành động", example = "Làm lại bộ đề SDLC Testing")
        private String description;

        @JsonProperty("action_items")
        @Schema(description = "Danh sách hành động cụ thể")
        private List<String> actionItems;

        @JsonProperty("estimated_time")
        @Schema(description = "Thời gian ước tính", example = "2 days")
        private String estimatedTime;

        @Schema(description = "Mức ưu tiên", example = "HIGH")
        private String priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamReadiness {

        @JsonProperty("current_score")
        @Schema(description = "Điểm hiện tại", example = "600")
        private Integer currentScore;

        @JsonProperty("projected_score")
        @Schema(description = "Điểm dự kiến", example = "700")
        private Integer projectedScore;

        @JsonProperty("pass_likelihood")
        @Schema(description = "Xác suất đậu", example = "LOW")
        private String passLikelihood;

        @JsonProperty("days_until_ready")
        @Schema(description = "Số ngày dự kiến để sẵn sàng", example = "14")
        private Integer daysUntilReady;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {

        @Schema(description = "Phản hồi lấy từ cache hay không", example = "true")
        private Boolean cached;

        @Schema(description = "Thông tin usage/tokens từ AI")
        private Usage usage;

        @JsonProperty("generated_at")
        @Schema(description = "Thời gian AI sinh ra kết quả", example = "2025-01-01T12:00:00Z")
        private String generatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {

        @Schema(description = "Tên model", example = "gpt-4o-mini")
        private String model;

        @JsonProperty("input_tokens")
        @Schema(description = "Số token input", example = "1234")
        private Integer inputTokens;

        @JsonProperty("output_tokens")
        @Schema(description = "Số token output", example = "567")
        private Integer outputTokens;

        @JsonProperty("cost_usd")
        @Schema(description = "Chi phí ước tính (USD)", example = "0.0025")
        private Double costUsd;
    }
}

