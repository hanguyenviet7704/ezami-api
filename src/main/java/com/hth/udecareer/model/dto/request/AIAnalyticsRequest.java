package com.hth.udecareer.model.dto.request;

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
@Schema(description = "Payload gửi sang service AI phân tích readiness/weakness")
public class AIAnalyticsRequest {

    @JsonProperty("user_id")
    @Schema(description = "ID người dùng", example = "123")
    private Long userId;

    @JsonProperty("total_attempts")
    @Schema(description = "Tổng số lần làm bài", example = "300")
    private Long totalAttempts;

    @JsonProperty("overall_accuracy")
    @Schema(description = "Tỉ lệ đúng tổng thể (%)", example = "62.5")
    private Double overallAccuracy;

    @JsonProperty("readiness_score")
    @Schema(description = "Điểm readiness tổng hợp", example = "58")
    private Integer readinessScore;

    @JsonProperty("chapter_stats")
    @Schema(description = "Danh sách thống kê theo chương/chapter")
    private List<ChapterStat> chapterStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterStat {
        @Schema(description = "Chỉ số chương dạng chuỗi", example = "1")
        private String chapter;

        @Schema(description = "Độ chính xác (%)", example = "80.0")
        private Double accuracy;

        @Schema(description = "Trạng thái đánh giá", example = "GOOD")
        private String status;
    }
}

