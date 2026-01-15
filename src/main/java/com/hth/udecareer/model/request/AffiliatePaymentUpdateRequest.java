package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Update payment information for the current affiliate.
 * Supports bank transfer today; can be extended for PayPal/Stripe later.
 */
@Data
public class AffiliatePaymentUpdateRequest {

    @Size(max = 30, message = "AFFILIATE_BANK_ACCOUNT_MAX_30")
    private String bankAccountNumber;

    @Size(max = 50, message = "AFFILIATE_BANK_NAME_MAX_50")
    private String bankName;

    @Pattern(regexp = "^[0-9]{10}$", message = "AFFILIATE_PHONE_INVALID")
    private String phone;
}

