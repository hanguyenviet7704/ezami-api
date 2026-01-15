package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Chi tiết kết quả làm bài quiz - từng câu hỏi với đáp án và giải thích")
public class QuizResultDetailResponse {

    @Schema(description = "ID của activity (lần làm bài)", example = "12345")
    private Long activityId;

    @Schema(description = "ID của quiz", example = "123")
    private Long quizId;

    @Schema(description = "Tên quiz", example = "PSM I - Full Test 1")
    private String quizTitle;

    @Schema(description = "Thông tin tóm tắt kết quả")
    private ResultSummary summary;

    @Schema(description = "Danh sách chi tiết từng câu hỏi")
    private List<QuestionDetail> questions;

    @Data
    @Builder
    @Schema(description = "Tóm tắt kết quả làm bài")
    public static class ResultSummary {
        @Schema(description = "Tổng số câu hỏi", example = "100")
        private Integer totalQuestions;

        @Schema(description = "Số câu đã trả lời", example = "95")
        private Long answeredQuestions;

        @Schema(description = "Số câu đúng", example = "85")
        private Long correctAnswers;

        @Schema(description = "Số câu sai", example = "10")
        private Long incorrectAnswers;

        @Schema(description = "Số câu bỏ qua", example = "5")
        private Integer skippedQuestions;

        @Schema(description = "Điểm đạt được", example = "850")
        private Long score;

        @Schema(description = "Tổng điểm", example = "1000")
        private Long totalPoints;

        @Schema(description = "Phần trăm", example = "85.5")
        private Double percentage;

        @Schema(description = "Trạng thái pass/fail", example = "true")
        private Boolean pass;

        @Schema(description = "Thời gian làm bài (giây)", example = "7200")
        private Long timeSpent;
    }

    @Data
    @Builder
    @Schema(description = "Chi tiết một câu hỏi trong kết quả")
    public static class QuestionDetail {
        @Schema(description = "ID câu hỏi", example = "456")
        private Long questionId;

        @Schema(description = "Số thứ tự câu hỏi", example = "1")
        private Integer questionNumber;

        @Schema(description = "Tiêu đề/Mã câu hỏi", example = "TOEIC_RC_PART5_G1_Q1")
        private String questionTitle;

        @Schema(description = "Nội dung câu hỏi (HTML)", example = "<p>The company's new policy will be ____ next month.</p>")
        private String questionText;

        @Schema(description = "Điểm của câu hỏi", example = "10")
        private Integer questionPoints;

        @Schema(description = "Danh sách các đáp án", example = "[{\"index\": 0, \"text\": \"implement\", \"isCorrect\": false}, ...]")
        private List<AnswerOption> answerOptions;

        @Schema(description = "Câu trả lời có đúng không", example = "false")
        private Boolean isCorrect;

        @Schema(description = "Điểm đạt được cho câu này", example = "0")
        private Long earnedPoints;

        @Schema(description = "Giải thích câu trả lời đúng", example = "Đáp án đúng là 'implemented' vì...")
        private String correctExplanation;

        @Schema(description = "Giải thích tại sao sai (nếu user chọn sai)", example = "Bạn đã chọn 'implement' nhưng cần dùng thì quá khứ")
        private String incorrectExplanation;
    }

    @Data
    @Builder
    @Schema(description = "Một đáp án trong câu hỏi")
    public static class AnswerOption {
        @Schema(description = "Chỉ số đáp án (0, 1, 2, 3)", example = "0")
        private Integer index;

        @Schema(description = "Nội dung đáp án", example = "implement")
        private String text;

        @Schema(description = "Đây có phải đáp án đúng không", example = "false")
        private Boolean isCorrect;

        @Schema(description = "User có chọn đáp án này không", example = "true")
        private Boolean isSelected;
    }
}
