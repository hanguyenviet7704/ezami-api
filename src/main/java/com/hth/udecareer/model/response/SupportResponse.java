package com.hth.udecareer.model.response;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
public class SupportResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String adminNote;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
