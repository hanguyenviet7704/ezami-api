package com.hth.udecareer.model.webhook;

import lombok.Data;

@Data
public class SePayWebhookDto {
    // id may be uuid-like string coming from provider (non-numeric), keep as String to be tolerant
    private String id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType;
    private Long transferAmount;
    private Long accumulated;
    private String subAccount;
    private String referenceCode;
    private String description;
}
