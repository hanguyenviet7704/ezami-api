package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponse {
    private Long id;
    private String actionName;
    private String actionDescription;
    private Integer points;
    private String message;
    private Long feedId;
    private Long relatedId;
    private LocalDateTime createdAt;
}

