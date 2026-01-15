package com.hth.udecareer.mapper;

import com.hth.udecareer.entities.Lesson;
import com.hth.udecareer.model.response.LessonDetailResponse;
import com.hth.udecareer.model.response.LessonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    LessonResponse toLessonResponse (Lesson lesson);
    LessonDetailResponse toLessonDetailResponse(Lesson lesson);
}
