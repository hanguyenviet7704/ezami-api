package com.hth.udecareer.model.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class PointInfoDto {
    private long totalPoints;

    private long points;

    private String type;
}
