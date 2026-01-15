package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankBenefitResponse {
    private String tierName;
    private String range;
    private String description;
    private String iconUrl;
}
