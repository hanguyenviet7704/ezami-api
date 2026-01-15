package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePreResponse {
    private Long id;
    private String title;
    private LocalDateTime date;

    @Builder.Default
    private String imageUrl =
            "https://www.shutterstock.com/image-photo/education-abbreviation-edu-pencil-paper-260nw-2644542615.jpg";

    @Builder.Default
    private float price = 100;

    private ReviewResponse reviewResponse;

    private Integer completedPercent;

}
