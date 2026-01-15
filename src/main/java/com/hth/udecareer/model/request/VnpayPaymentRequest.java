package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "Thông tin yêu cầu khởi tạo thanh toán VNPAY")
public class VnpayPaymentRequest {

    @NotBlank(message = "Category code is required")
    @Schema(
            description = """
            Mã danh mục khóa học/quiz cần mua.
            
            **Các mã hợp lệ (lấy từ DB):**
            - `psm1` (PSM I)
            - `psm2` (PSM II)
            - `pspo1` (PSPO I)
            - `pspo2` (PSPO II)
            - `istqb_foundation` (ISTQB Foundation)
            - `istqb_adv_ta` (ISTQB Advanced - Test Analyst)
            - `istqb_adv_tm` (ISTQB Advanced - Test Manager)
            - `istqb_adv_tta` (ISTQB Advanced - Technical Test Analyst)
            - `ccba` (CCBA)
            - `ecba` (ECBA)
            - `cbap` (CBAP)
            - `istqb_agile` (ISTQB Agile)
            """,
            example = "psm1",
            required = true
    )
    private String categoryCode;

    @NotBlank(message = "Plan code is required")
    @Schema(
            description = "Mã gói dịch vụ (Giá và Thời hạn). Ví dụ: `PLAN_30`, `PLAN_90`",
            example = "PLAN_30",
            required = true
    )
    private String planCode;

    @Schema(
            description = "Mã voucher giảm giá (tùy chọn). Nếu có, hệ thống sẽ áp dụng giảm giá trước khi thanh toán.",
            example = "SUMMER2025",
            required = false
    )
    private String voucherCode;

}