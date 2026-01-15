package com.hth.udecareer.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LanguagePointInfoDto extends PointInfoDto {
    private long listeningPoints;

    private long listeningCorrects;

    private long readingPoints;

    private long readingCorrects;

    private List<PartPoint> partPoints;

    @Data
    @Builder
    public static class PartPoint {
        private String partCode;

        private String partName;

        private long corrects;

        private long questions;

        private long points;

        private long totalPoints;
    }
}
