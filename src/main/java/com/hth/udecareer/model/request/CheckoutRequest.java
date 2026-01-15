package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin yêu cầu checkout giỏ hàng")
public class CheckoutRequest {

    @Schema(
            description = "Mã voucher giảm giá (tùy chọn). Nếu có, hệ thống sẽ áp dụng giảm giá trước khi thanh toán.",
            example = "SUMMER2025"
    )
    private String voucherCode;
}
