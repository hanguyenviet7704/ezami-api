package com.hth.udecareer.model.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {

    private String voucherId;
    private String title;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String status;
    private String conditions;
    private String description;

    public VoucherResponse(String voucherId, String title, String code,
                           String discountType, BigDecimal discountValue,
                           LocalDate validFrom, LocalDate validTo, String status) {
        this.voucherId = voucherId;
        this.title = title;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.status = status;
    }
}
