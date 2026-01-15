package com.hth.udecareer.model.response;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.hth.udecareer.entities.QuizCategoryEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin danh mục quiz/bài kiểm tra")
public class CategoryResponse {
    @Schema(description = "ID duy nhất của danh mục", example = "1")
    private Long id;

    @Schema(description = "Mã kỳ thi", example = "TOEIC")
    private String examCode;

    @Schema(description = "Mã danh mục", example = "toeic-reading")
    private String code;

    @Schema(description = "Tiêu đề danh mục", example = "TOEIC Reading")
    private String title;

    @Schema(description = "Header mô tả danh mục", example = "Luyện thi TOEIC Reading")
    private String header;

    @Schema(description = "URL hình ảnh đại diện cho danh mục", example = "https://example.com/toeic.jpg")
    private String imageUri;

    @Schema(description = "Số lượng bài test đầy đủ", example = "10")
    private Integer numFullTest;

    @Schema(description = "Số lượng bài test ngắn", example = "50")
    private Integer numMiniTest;

    @Schema(description = "Map số lượng test theo loại", example = "{\"full\": 10, \"mini\": 50}")
    private Map<String, Integer> numTest;

    @Schema(description = "Mã ưu đãi (offer ID) từ RevenueCat", example = "rc_toeic_monthly")
    private String offer;

    @Schema(description = "Mã quyền truy cập (entitlement) từ RevenueCat", example = "premium")
    private String entitlement;

    @Schema(description = "Thông tin về việc người dùng đã mua danh mục này chưa")
    private PurchasedInfo purchasedInfo;

    @Schema(description = "Số lượng danh mục con", example = "3")
    private long numChildren;

    @Schema(description = "Danh mục này có miễn phí không", example = "false")
    private boolean free;

    @Data
    @Builder
    @Schema(description = "Thông tin mua hàng của người dùng cho danh mục này")
    public static class PurchasedInfo {
        @Schema(description = "Người dùng đã mua chưa", example = "true")
        private Boolean isPurchased;

        @Schema(description = "Thời gian bắt đầu (Unix timestamp)", example = "1609459200000")
        private Long fromTime;

        @Schema(description = "Thời gian kết thúc (Unix timestamp)", example = "1640995200000")
        private Long toTime;
    }

    public static CategoryResponse from(String code, String title, String imageUri) {
        return builder()
                .code(code)
                .title(title)
                .imageUri(imageUri)
                .build();
    }

    public static CategoryResponse from(String code, String header, String title, String imageUri) {
        return builder()
                .code(code)
                .header(header)
                .title(title)
                .imageUri(imageUri)
                .build();
    }

    public static CategoryResponse from(@NotNull final QuizCategoryEntity entity) {
        return builder()
                .code(entity.getCode())
                .title(entity.getTitle())
                .header(entity.getHeader())
                .imageUri(entity.getImageUri())
                .build();
    }

    public static CategoryResponse from(String code, String title, String imageUri, String offer,
                                        String entitlement) {
        return builder()
                .code(code)
                .title(title)
                .imageUri(imageUri)
                .offer(offer)
                .entitlement(entitlement)
                .build();
    }
}
