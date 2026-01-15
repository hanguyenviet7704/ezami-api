package com.hth.udecareer.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppLogResponse {
    @Builder.Default
    private String message = "App log saved successfully";
}
