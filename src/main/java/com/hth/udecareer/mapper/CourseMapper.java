package com.hth.udecareer.mapper;

import com.hth.udecareer.entities.Course;
import com.hth.udecareer.model.response.CoursePreResponse;
import com.hth.udecareer.model.response.CourseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "reviewResponse.totalCount", source = "commentCount")
    @Mapping(target = "reviewResponse.averageScore", constant = "5.0")
    CourseResponse toCourseResponse (Course course);

    @Mapping(target = "reviewResponse.totalCount", source = "commentCount")
    @Mapping(target = "reviewResponse.averageScore", constant = "5.0")
    CoursePreResponse toCoursePreResponse(Course course);
}
