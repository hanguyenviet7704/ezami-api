package com.hth.udecareer.model.webhook;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ContentAccessRequest {
    @NotBlank
    private String contentType; // course, lesson, quiz, topic, post

    @NotBlank
    private String contentId; // id or code

    // Provide either userId or userEmail to identify user
    private Long userId;
    private String userEmail;
}
