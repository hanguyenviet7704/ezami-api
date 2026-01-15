package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hth.udecareer.enums.FavoritableType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thông tin một favorite")
public class FavoriteResponse {

    @Schema(description = "ID của favorite", example = "123")
    private Long id;

    @Schema(description = "ID của user", example = "456")
    private Long userId;

    @Schema(description = "Loại nội dung yêu thích", example = "course")
    private String favoriteType;

    @Schema(description = "ID của nội dung yêu thích", example = "789")
    private Long favoriteId;

    @Schema(description = "Trạng thái favorite", example = "ACTIVE")
    private String status;

    @Schema(description = "Thời gian tạo favorite", example = "2025-11-13T10:30:00")
    private LocalDateTime createdAt;

    // Post info
    @Schema(description = "Tiêu đề bài viết", example = "JavaScript Basics")
    private String title;

    @Schema(description = "Trạng thái bài viết", example = "publish")
    private String postStatus;

    // Quiz Category info
    @Schema(description = "Mã category", example = "psm1")
    private String categoryCode;

    @Schema(description = "Tên category", example = "PSM I")
    private String categoryTitle;

    @Schema(description = "URL ảnh category", example = "https://www.ezami.vn/wp-content/uploads/2024/06/psm1_ezami_4.png")
    private String categoryImageUri;

    // Featured Image from wp_postmeta
    @Schema(description = "URL ảnh đại diện của post/course/lesson", example = "https://www.ezami.vn/wp-content/uploads/2024/course-image.jpg")
    private String featuredImage;
}
