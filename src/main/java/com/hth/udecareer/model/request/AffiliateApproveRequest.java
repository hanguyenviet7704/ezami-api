package com.hth.udecareer.model.request;

import lombok.Data;

@Data
public class AffiliateApproveRequest {
    Long affiliateId;
    Boolean approved;
    private String rejectReason;
}
