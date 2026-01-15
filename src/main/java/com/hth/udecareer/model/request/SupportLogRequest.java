package com.hth.udecareer.model.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportLogRequest {
    private String channel;
    private LocalDateTime createdAt;
}
