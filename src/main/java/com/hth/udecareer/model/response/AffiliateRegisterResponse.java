package com.hth.udecareer.model.response;

import com.hth.udecareer.enums.AffiliateStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateRegisterResponse {
    private AffiliateStatus status;
    private String rejectReason;
    private String affiliateCode;
}
