package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.Lesson;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserActivityEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.mapper.LessonMapper;
import com.hth.udecareer.model.response.LessonDetailResponse;
import com.hth.udecareer.repository.LessonRepository;
import com.hth.udecareer.repository.PostMetaRepository;
import com.hth.udecareer.repository.UserActivityRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final PostMetaRepository postMetaRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;

    private final LessonMapper lessonMapper;

    @Override
    public LessonDetailResponse getLesson(Long courseId, Long lessonId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // check relation
        if (!postMetaRepository
                .existsByPostIdAndMetaKeyAndMetaValue(lessonId, "course_id", String.valueOf(courseId))) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        UserActivityEntity userActivity = userActivityRepository.findByUserAndLessonAndCourse(user.getId(), lessonId, courseId).orElse(null);

        LessonDetailResponse lessonDetailResponse = lessonMapper.toLessonDetailResponse(lesson);
        if(userActivity==null){
            lessonDetailResponse.setCompleted(false);
        }else{
            lessonDetailResponse.setCompleted(userActivity.getActivityStatus() == 1);
        }


        return  lessonDetailResponse;
    }
}
