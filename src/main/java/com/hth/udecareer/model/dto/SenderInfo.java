package com.hth.udecareer.model.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SenderInfo {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
}
