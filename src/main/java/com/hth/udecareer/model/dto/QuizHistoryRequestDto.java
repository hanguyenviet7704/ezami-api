package com.hth.udecareer.model.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Data
public class QuizHistoryRequestDto {

    @Parameter(
            description = "ID của quiz cụ thể (tùy chọn). Nếu không truyền sẽ lấy tất cả quiz.",
            example = "123"
    )
    @Positive(message = "quizId must be positive")
    private Long quizId;

    @Parameter(
            description = "Mã danh mục quiz (code) - tùy chọn. Lọc theo category.",
            example = "PSM-I"
    )
    private String categoryCode;

    @Parameter(
            description = "Thời gian bắt đầu (Unix timestamp) - tùy chọn. Lọc các bài làm từ thời điểm này trở đi.",
            example = "1700000000"
    )
    @Positive(message = "fromDate must be positive")
    private Long fromDate;

    @Parameter(
            description = "Thời gian kết thúc (Unix timestamp) - tùy chọn. Lọc các bài làm đến thời điểm này.",
            example = "1730000000"
    )
    @Positive(message = "toDate must be positive")
    private Long toDate;

    @Parameter(
            description = "Sắp xếp theo trường. Các giá trị: 'time' (thời gian), 'score' (điểm số), 'percentage' (phần trăm). Mặc định: 'time'",
            example = "score"
    )
    @Pattern(regexp = "^(time|score|percentage)$", message = "sortBy must be: time, score, or percentage")
    private String sortBy = "time";

    @Parameter(
            description = "Hướng sắp xếp. Các giá trị: 'asc' (tăng dần), 'desc' (giảm dần). Mặc định: 'desc'",
            example = "desc"
    )
    @Pattern(regexp = "^(asc|desc)$", message = "sortDirection must be: asc or desc")
    private String sortDirection = "desc";

    @Parameter(
            description = "Số trang (bắt đầu từ 0). Mặc định: 0",
            example = "0"
    )
    @Min(value = 0, message = "page must be greater than or equal to 0")
    private Integer page = 0;

    @Parameter(
            description = "Kích thước trang (tối đa 100). Mặc định: 20",
            example = "20"
    )
    @Positive(message = "size must be positive")
    @Max(value = 100, message = "size must not exceed 100")
    private Integer size = 20;
}
