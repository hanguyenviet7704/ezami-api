package com.hth.udecareer.model.response;

import lombok.Data;

@Data
public class LessonProgressResponse {
    private Long lessonId;
    private Long lastPosition;
    private Boolean completed;
    private Long courseId;
    private Integer completedPercent;
}
