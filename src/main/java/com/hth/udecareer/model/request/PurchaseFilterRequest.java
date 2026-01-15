package com.hth.udecareer.model.request;

import java.time.LocalDate;

import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa tất cả filter parameters cho Purchase History API
 * Tất cả fields đều OPTIONAL - cho phép combine nhiều filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter parameters cho Purchase History API - Tất cả đều optional")
public class PurchaseFilterRequest {

    @Schema(description = "User ID (lấy từ JWT token)", example = "1", hidden = true)
    private Long userId;

    @Schema(description = "Từ khóa tìm kiếm trong category title", example = "ECBA")
    private String keyword;

    @Schema(description = "Lọc theo trạng thái: true = còn hạn, false = hết hạn, null = tất cả", example = "true")
    private Boolean isActive;

    @Schema(description = "Lọc purchases từ ngày này (format: yyyy-MM-dd)", example = "2024-01-01")
    private LocalDate fromDate;

    @Schema(description = "Lọc purchases đến ngày này (format: yyyy-MM-dd)", example = "2024-12-31")
    private LocalDate toDate;

    /**
     * Kiểm tra có filter nào được apply không (ngoài userId)
     */
    public boolean hasAnyFilter() {
        return (keyword != null && !keyword.trim().isEmpty())
                || isActive != null
                || fromDate != null
                || toDate != null;
    }

    /**
     * Validate date range and required fields
     * @throws AppException with HTTP 400 for invalid input
     */
    public void validate() throws AppException {
        // Validate userId (required)
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_KEY); // hoặc tạo USER_ID_REQUIRED nếu cần
        }

        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
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
        return "PurchaseFilterRequest{" +
                "userId=" + userId +
                ", keyword='" + keyword + '\'' +
                ", isActive=" + isActive +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
