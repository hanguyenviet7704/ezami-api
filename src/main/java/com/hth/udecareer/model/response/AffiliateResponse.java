package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hth.udecareer.enums.AffiliateStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AffiliateResponse {
    private Long id;
    private Long userId;
    private String affiliateCode;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String bankAccountNumber;
    private String bankName;
    private String website;
    private String promotionMethod;
    private AffiliateStatus status;
    private String rejectReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
}
