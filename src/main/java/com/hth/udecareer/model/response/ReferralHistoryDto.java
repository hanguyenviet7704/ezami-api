package com.hth.udecareer.model.response;

import com.hth.udecareer.enums.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralHistoryDto {
    private Long id;
    private LocalDateTime createdAt;
    private String orderId;              // Masked: ****123
    private String productNames;         // Comma-separated product names
    private BigDecimal orderAmount;
    private BigDecimal commissionAmount;
    private CommissionStatus status;
    private String reason;               // Rejection reason if applicable
}

