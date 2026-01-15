package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AffiliateRegisterRequest {
    @NotBlank(message = "AFFILIATE_FIRST_NAME_REQUIRED")
    @Size(max = 100, message = "AFFILIATE_FIRST_NAME_MAX_100")
    private String firstName;

    @Size(max = 100, message = "AFFILIATE_LAST_NAME_MAX_100")
    private String lastName;

    @Size(max = 30, message = "AFFILIATE_BANK_ACCOUNT_MAX_30")
    private String bankAccountNumber;

    @Size(max = 50, message = "AFFILIATE_BANK_NAME_MAX_50")
    private String bankName;

    @NotBlank(message = "AFFILIATE_PHONE_REQUIRED")
    @Pattern(regexp = "^[0-9]{10}$", message = "AFFILIATE_PHONE_INVALID")
    private String phone;

    @Size(max = 255, message = "AFFILIATE_WEBSITE_MAX_255")
    private String website;

    @Size(max = 255, message = "AFFILIATE_PROMOTION_METHOD_MAX_255")
    private String promotionMethod;
}
