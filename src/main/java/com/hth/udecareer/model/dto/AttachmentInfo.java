package com.hth.udecareer.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin attachment (image)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentInfo {
    private Long id;
    private String url;
    private String title;
}
