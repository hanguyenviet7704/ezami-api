package com.hth.udecareer.model.webhook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentAccessResponse {
    private boolean hasAccess;
    private String message;
    private LocalDateTime expiresAt; // optional
}
