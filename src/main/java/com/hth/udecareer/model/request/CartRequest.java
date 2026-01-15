package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CartRequest {
    @NotBlank(message = "Category code is required")
    @Schema(
            description = "Mã danh mục khóa học muốn mua (Lấy từ API /quiz/category)",
            example = "psm1",
            required = true
    )
    private String categoryCode;

    @NotBlank(message = "Plan code is required")
    @Schema(
            description = "Mã gói dịch vụ (Quy định giá và thời hạn)",
            example = "PLAN_30",
            required = true
    )
    private String planCode;
}