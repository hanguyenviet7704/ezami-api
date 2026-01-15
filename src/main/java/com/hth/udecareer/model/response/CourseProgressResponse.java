package com.hth.udecareer.model.response;

import lombok.Data;

import java.util.List;

@Data
public class CourseProgressResponse {
    private Long courseId;
    private Integer completedPercent;
    private List<LessonProgressResponse> lessons;
}
