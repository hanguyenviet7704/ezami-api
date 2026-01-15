package com.hth.udecareer.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String title;
    private String content;
    private String action;
    private Long objectId;
    private String route;
    private Long srcUserId;
    private String srcObjectType;
    private Long feedId;
}

