package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin lịch sử làm bài quiz của người dùng")
public class QuizHistoryResponse {
    
    @Schema(description = "ID của activity (lần làm bài)", example = "12345")
    private Long activityId;

    @Schema(description = "ID của quiz", example = "123")
    private Long quizId;

    @Schema(description = "ID của post quiz", example = "74000")
    private Long postId;

    @Schema(description = "Tên của quiz", example = "PSM I - Full Test 1")
    private String quizTitle;

    @Schema(description = "Slug của quiz", example = "psm-i-full-test-1")
    private String quizSlug;

    @Schema(description = "Thông tin category của quiz", example = "{\"code\": \"PSM-I\", \"title\": \"PSM I\"}")
    private CategorySimple category;

    @Schema(description = "Loại quiz (mini/full)", example = "full")
    private String quizType;

    @Schema(description = "Thời gian bắt đầu làm bài (Unix timestamp)", example = "1700000000")
    private Long activityStarted;

    @Schema(description = "Thời gian hoàn thành bài (Unix timestamp)", example = "1700008100")
    private Long activityCompleted;

    @Schema(description = "Thời gian làm bài (giây)", example = "8100")
    private Long timeSpent;

    @Schema(description = "Điểm số đạt được", example = "850")
    private Long score;

    @Schema(description = "Tổng điểm tối đa", example = "1000")
    private Long totalPoints;

    @Schema(description = "Số câu trả lời đúng", example = "85")
    private Long correctAnswers;

    @Schema(description = "Số câu trả lời sai", example = "15")
    private Long incorrectAnswers;

    @Schema(description = "Số câu đã trả lời", example = "95")
    private Long answeredQuestions;

    @Schema(description = "Tổng số câu hỏi", example = "100")
    private Long totalQuestions;

    @Schema(description = "Phần trăm điểm đạt được", example = "85.5")
    private Double percentage;

    @Schema(description = "Trạng thái đạt/không đạt (true: đạt, false: không đạt)", example = "true")
    private Boolean pass;

    @Schema(description = "Phần trăm cần đạt để pass", example = "70")
    private Integer passingPercentage;

    @Schema(description = "Có phải bài làm dở (draft) không", example = "false")
    private Boolean isDraft;
}
