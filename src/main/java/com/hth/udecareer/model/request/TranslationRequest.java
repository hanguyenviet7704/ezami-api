package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để tạo hoặc cập nhật translation")
public class TranslationRequest {

    @NotBlank(message = "VALIDATION_ERROR")
    @Size(max = 50, message = "VALIDATION_ERROR")
    @Schema(description = "Loại entity (badge, space, topic, question, notification)",
            example = "badge", required = true)
    private String entityType;

    @NotNull(message = "VALIDATION_ERROR")
    @Schema(description = "ID của entity cần dịch", example = "1", required = true)
    private Long entityId;

    @NotBlank(message = "VALIDATION_ERROR")
    @Size(max = 50, message = "VALIDATION_ERROR")
    @Schema(description = "Tên field cần dịch (name, title, description, content, message)",
            example = "name", required = true)
    private String fieldName;

    @NotBlank(message = "VALIDATION_ERROR")
    @Size(max = 5, message = "VALIDATION_ERROR")
    @Schema(description = "Mã ngôn ngữ (vi, en)", example = "en", required = true)
    private String language;

    @Schema(description = "Giá trị đã dịch", example = "First Post Badge")
    private String translatedValue;
}
