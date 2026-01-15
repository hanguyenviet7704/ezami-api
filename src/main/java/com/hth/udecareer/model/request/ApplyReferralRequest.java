package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ApplyReferralRequest {
    @NotBlank(message = "REFERRAL_CODE_INVALID_FORMAT")
    private String referralCode;
}
