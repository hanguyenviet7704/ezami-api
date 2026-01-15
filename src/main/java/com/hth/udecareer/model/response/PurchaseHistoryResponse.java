package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistoryResponse {
    
    private Long id;
    private Long userId;
    private String userEmail;
    private String categoryCode;
    
    // Thông tin từ ez_quiz_category
    private String categoryTitle;
    private String categoryImageUri;
    
    private Boolean isPurchased;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fromTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime toTime;
    
    private Boolean isActive;
    private Integer daysRemaining;
}