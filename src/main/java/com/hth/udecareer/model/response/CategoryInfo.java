package com.hth.udecareer.model.response;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin danh mục (dùng trong response root)")
public class CategoryInfo {
    @Schema(description = "Mã danh mục", example = "PSM-I")
    private String code;

    @Schema(description = "Tên danh mục (để hiển thị header title)", example = "PSM I")
    private String title;

    @Schema(description = "Header/description (để hiển thị header description)", example = "Professional Scrum Master I")
    private String header;

    @Schema(description = "Số lượng test theo loại", example = "{\"full\": 10, \"mini\": 5}")
    private Map<String, Integer> numTest;
}

