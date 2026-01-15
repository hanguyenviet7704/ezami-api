package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hth.udecareer.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String content;
    private String title;
    private LocalDateTime date;
    private List<LessonResponse> lessons;

    @Builder.Default
    private String imageUrl =
            "https://www.shutterstock.com/image-photo/education-abbreviation-edu-pencil-paper-260nw-2644542615.jpg";

    @Builder.Default
    private float price = 100;

    private ReviewResponse reviewResponse;
}
