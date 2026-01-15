package com.hth.udecareer.model.dto;

import com.hth.udecareer.annotation.validation.ValidSortField;
import com.hth.udecareer.annotation.validation.MinMaxTimeLimit;
import com.hth.udecareer.annotation.validation.ValidCategoryCode;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import javax.validation.constraints.*; // Đảm bảo import @Min và @Max

@Data
@MinMaxTimeLimit(message = "minTimeLimit must be less than or equal to maxTimeLimit")
public class QuizSearchRequestDto {

    @Parameter(
            description = "Mã danh mục quiz (title) - tùy chọn. Nếu có categoryCode thì bỏ qua field này",
            example = "PSM I"
    )
    @ValidCategoryCode
    private String category;

    @Parameter(
            description = "Mã danh mục quiz (code) - tùy chọn. Nếu có thì ưu tiên dùng thay cho category (title)",
            example = "PSM-I"
    )
    private String categoryCode;

    @Parameter(
            description = "Lọc quiz theo ID của Khóa học (tùy chọn)",
            example = "74000"
    )
    @Positive(message = "courseId must be positive")
    private Long courseId;

    @Parameter(
            description = "Loại bài test (tùy chọn). Các giá trị: 'mini', 'full', 'all'. Mặc định: 'all'",
            example = "mini"
    )
    @Pattern(regexp = "^(mini|full|all)$", message = "quizType must be: mini, full, or all")
    private String quizType = "all";

    @Parameter(
            description = "Thời gian tối thiểu (giây). Lọc quiz có timeLimit >= giá trị này",
            example = "1800" // (ví dụ: 30 phút)
    )
    @Positive(message = "minTimeLimit must be positive")
    @Max(value = 86400, message = "minTimeLimit must not exceed 86400 seconds (24 hours)")
    private Integer minTimeLimit;

    @Parameter(
            description = "Thời gian tối đa (giây). Lọc quiz có timeLimit <= giá trị này",
            example = "7200" // (ví dụ: 120 phút)
    )
    @Positive(message = "maxTimeLimit must be positive")
    @Max(value = 86400, message = "maxTimeLimit must not exceed 86400 seconds (24 hours)")
    private Integer maxTimeLimit;

    @Parameter(
            description = "Sắp xếp theo trường. Format: 'field,direction'. Fields: id, name, timeLimit, slug, postId, postTitle",
            example = "id,desc"
    )
    @ValidSortField
    private String sort;

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