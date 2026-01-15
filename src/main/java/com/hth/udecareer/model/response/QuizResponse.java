package com.hth.udecareer.model.response;

import java.util.Map;

import com.hth.udecareer.model.dto.QuizDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin bài quiz/bài kiểm tra")
public class QuizResponse {
    @Schema(description = "ID duy nhất của quiz", example = "123")
    private Long id;

    @Schema(description = "Slug của quiz (dùng cho URL )", example = "toeic-reading-test-1")
    private String slug;

    @Schema(description = "Thời gian giới hạn làm bài (phút)", example = "60")
    private Integer timeLimit;

    @Schema(description = "Tổng số câu hỏi trong quiz", example = "100")
    private Long questions;

    @Schema(description = "Phần trăm điểm tối thiểu để đạt (pass)", example = "70")
    private Integer passingPercentage;

    @Schema(description = "Số câu hỏi người dùng đã trả lời", example = "95")
    private Long answeredQuestions;

    @Schema(description = "Tổng điểm người dùng đã đạt được", example = "850")
    private Long answeredPoints;

    @Schema(description = "Tổng điểm tối đa của quiz", example = "990")
    private Long totalPoints;

    @Schema(description = "Phần trăm điểm đạt được", example = "85.85")
    private Double percentage;

    @Schema(description = "Số câu trả lời đúng", example = "85")
    private Long answeredCorrects;

    @Schema(description = "Điểm số người dùng đạt được", example = "850")
    private Long answeredScore;

    @Schema(description = "Trạng thái đã đạt hay chưa (1: đạt, 0: chưa đạt)", example = "1")
    private Long pass;

    @Schema(description = "Tên của quiz", example = "TOEIC Reading Practice Test 1")
    private String name;

    @Schema(description = "ID của bài post liên quan", example = "456")
    private Long postId;

    @Schema(description = "Nội dung HTML của bài post", example = "<p>Đây là bài test TOEIC Reading...</p>")
    private String postContent;

    @Schema(description = "Tiêu đề của bài post", example = "TOEIC Reading Practice Test 1")
    private String postTitle;


    @Schema(description = "Loại quiz", example = "mini")
    private String quizType;

    @Schema(description = "Có bản draft hay không (true: có draft, false: không có)", example = "true")
    private Boolean isDraft;

    @Schema(description = "Thông tin draft (nếu có). Chứa thông tin chung về bài làm dở: activityStartTime, elapsedTime, answeredCount (không có answers)", example = "{\"activityStartTime\": 1704067200, \"elapsedTime\": 1800, \"answeredCount\": 50}")
    private Map<String, Object> savedAnswers;

    @Schema(description = "Thông tin danh mục của quiz (để hiển thị category tag)", example = "{\"code\": \"PSM-I\", \"title\": \"PSM I\"}")
    private CategorySimple category;

    @Schema(description = "Trạng thái đã mua gói quiz hay chưa (true: đã mua, false: chưa mua)", example = "true")
    private Boolean isPurchased;

    public static QuizResponse from(QuizDto quizDto) {
        return builder()
                .id(quizDto.getId())
                .slug(quizDto.getSlug())
                .timeLimit(quizDto.getTimeLimit())
                .name(quizDto.getName())
                .postId(quizDto.getPostId())
                .postContent(quizDto.getPostContent())
                .postTitle(quizDto.getPostTitle())
                .isDraft(false) // Default value
                .savedAnswers(null) // Default value
                .isPurchased(false) // Default value - will be updated by service if applicable
                .build();
    }
}
