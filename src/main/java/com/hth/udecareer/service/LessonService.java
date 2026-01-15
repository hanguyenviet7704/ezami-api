package com.hth.udecareer.service;

import com.hth.udecareer.model.response.LessonDetailResponse;

public interface LessonService {
    LessonDetailResponse getLesson(Long courseId, Long lessonId, String email);
}
