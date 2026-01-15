package com.hth.udecareer.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportLogResponse {
    @Builder.Default
    private String message = "Support log saved successfully";
}
