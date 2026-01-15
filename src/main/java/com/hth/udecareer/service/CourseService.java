package com.hth.udecareer.service;

import com.hth.udecareer.model.request.CourseNoteRequest;
import com.hth.udecareer.model.response.CourseNoteResponse;
import com.hth.udecareer.model.response.CoursePreResponse;
import com.hth.udecareer.model.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface CourseService {
    CourseResponse getCourseInfo(Long courseId, String email);
    List<CoursePreResponse> getCourses(Map<String, Object> params);
    Page<CoursePreResponse> getCoursesUsingPagination(Map<String, Object> params, Pageable pageable, @Nullable String email);
    CourseNoteResponse createOrUpdateCourseNote(Long courseId, CourseNoteRequest request, Principal principal);
    CourseNoteResponse getCourseNoteResponse(Long courseId, Principal principal);
}
