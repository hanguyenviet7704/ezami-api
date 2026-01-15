package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Course;
import com.hth.udecareer.model.response.CourseResponse;
import com.hth.udecareer.repository.custom.CourseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {
    List<Course> findAllByType(String type);
}
