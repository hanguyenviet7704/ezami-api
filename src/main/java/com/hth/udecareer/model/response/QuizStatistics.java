package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Số liệu thống kê tổng hợp của người dùng")
public class QuizStatistics {

    @Schema(description = "Tổng số bài thi đã làm", example = "50")
    private Long totalAttempts;

    @Schema(description = "Số bài thi đạt (Passed)", example = "35")
    private Long passedAttempts;

    @Schema(description = "Tỷ lệ đạt (%)", example = "70.0")
    private Double passRate;

    @Schema(description = "Điểm số/Phần trăm trung bình", example = "82.5")
    private Double averageScore;

    @Schema(description = "Thời gian làm bài trung bình (giây)", example = "1500")
    private Long averageTimeSpent;
}