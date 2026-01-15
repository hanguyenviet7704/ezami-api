package com.hth.udecareer.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportProcessRequest {
    private Long supportId;
    private String replyMessage;
}
