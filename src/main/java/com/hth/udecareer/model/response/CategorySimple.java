package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin danh mục đơn giản (dùng trong quiz item)")
public class CategorySimple {
    @Schema(description = "Mã danh mục", example = "PSM-I")
    private String code;

    @Schema(description = "Tên danh mục", example = "PSM I")
    private String title;
}

