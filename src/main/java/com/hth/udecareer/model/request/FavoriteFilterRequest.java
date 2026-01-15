package com.hth.udecareer.model.request;

import com.hth.udecareer.enums.FavoritableType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để lọc favorites")
public class FavoriteFilterRequest {

    @Schema(description = "Loại nội dung yêu thích (course, lesson, quiz, topic, post)", example = "course")
    private FavoritableType type;

    @Schema(description = "Từ ngày (yyyy-MM-dd)", example = "2024-01-01")
    private LocalDate fromDate;

    @Schema(description = "Đến ngày (yyyy-MM-dd)", example = "2024-12-31")
    private LocalDate toDate;

    @Schema(description = "Từ khóa tìm kiếm trong post_title, post_content, category title", example = "javascript")
    private String keyword;

    @Schema(description = "Số trang (0-based)", example = "0")
    private Integer page;

    @Schema(description = "Số items mỗi trang", example = "20")
    private Integer size;

    @Schema(description = "Sắp xếp (field,direction), ví dụ: createdAt,desc", example = "createdAt,desc")
    private String sort;
}
