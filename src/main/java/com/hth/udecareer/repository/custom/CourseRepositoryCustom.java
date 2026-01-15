package com.hth.udecareer.repository.custom;

import com.hth.udecareer.entities.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CourseRepositoryCustom {
    List<Course> findAllCoursesByConditions(Map<String, Object> conditions);

    Page<Course> findAllCoursesByConditions(Map<String, Object> conditions, Pageable pageable);
}
