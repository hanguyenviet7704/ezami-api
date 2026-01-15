package com.hth.udecareer.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class PurchasedDto {
    private boolean purchased;

    private Long remainDays;

    public static PurchasedDto from(Boolean isPurchased, Long remainDays) {
        return builder()
                .purchased(Optional.ofNullable(isPurchased).orElse(false))
                .remainDays(remainDays)
                .build();
    }

    public static PurchasedDto empty() {
        return builder()
                .purchased(false)
                .remainDays(0L)
                .build();
    }
}
