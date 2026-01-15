package com.hth.udecareer.model.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa tất cả filter parameters cho Post API
 * Tất cả fields đều OPTIONAL - cho phép combine nhiều filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter parameters cho Post API - Tất cả đều optional")
public class PostFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm (search trong title và content)", example = "wordpress")
    private String keyword;

    @Schema(description = "Danh sách IDs của categories cần lọc (có thể truyền nhiều IDs)", example = "[5, 10, 15]")
    private List<Long> categoryIds;

    @Schema(description = "ID của tác giả", example = "1")
    private Long authorId;

    @Schema(description = "Danh sách tag slugs để lọc", example = "[\"tutorial\", \"beginner\"]")
    private List<String> tags;

    @Schema(description = "Lọc posts từ ngày này (format: yyyy-MM-dd)", example = "2024-01-01")
    private LocalDate fromDate;

    @Schema(description = "Lọc posts đến ngày này (format: yyyy-MM-dd)", example = "2024-12-31")
    private LocalDate toDate;

    /**
     * Kiểm tra có filter nào được apply không
     */
    public boolean hasAnyFilter() {
        return (keyword != null && !keyword.trim().isEmpty())
                || (categoryIds != null && !categoryIds.isEmpty())
                || authorId != null
                || (tags != null && !tags.isEmpty())
                || fromDate != null
                || toDate != null;
    }

    /**
     * Validate date range
     */
    public void validate() {
        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        // Trim whitespace
        if (keyword != null) {
            keyword = keyword.trim();
        }
    }

    /**
     * Log-friendly string
     */
    @Override
    public String toString() {
        return "PostFilterRequest{" +
                "keyword='" + keyword + '\'' +
                ", categoryIds=" + categoryIds +
                ", authorId=" + authorId +
                ", tags=" + tags +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
