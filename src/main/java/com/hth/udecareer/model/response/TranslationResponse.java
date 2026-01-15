package com.hth.udecareer.model.response;

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
@Schema(description = "Response chứa thông tin translation")
public class TranslationResponse {

    @Schema(description = "ID của translation", example = "1")
    private Long id;

    @Schema(description = "Loại entity", example = "badge")
    private String entityType;

    @Schema(description = "ID của entity", example = "1")
    private Long entityId;

    @Schema(description = "Tên field", example = "name")
    private String fieldName;

    @Schema(description = "Mã ngôn ngữ", example = "en")
    private String language;

    @Schema(description = "Giá trị đã dịch", example = "First Post Badge")
    private String translatedValue;

    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;
}
