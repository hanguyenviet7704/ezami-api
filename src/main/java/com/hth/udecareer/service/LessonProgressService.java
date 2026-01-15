package com.hth.udecareer.service;

import com.hth.udecareer.entities.PostMeta;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserActivityEntity;
import com.hth.udecareer.entities.UserCourseEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.LessonProgressRequest;
import com.hth.udecareer.model.response.CourseProgressResponse;
import com.hth.udecareer.model.response.LessonProgressResponse;
import com.hth.udecareer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonProgressService {
    private final PostMetaRepository postMetaRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserActivityMetaRepository userActivityMetaRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;

    @Transactional
    public LessonProgressResponse updateLessonProgress(String email, LessonProgressRequest request, Long lessonId) {
        Long lastSecond = request.getLastSecond();
        Long duration = request.getDuration();


        Long userId = userRepository.findByEmail(email).map(u -> u.getId()).orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        PostMeta course = postMetaRepository.findByPostIdAndMetaKey(lessonId, "course_id");
        postMetaRepository.findDurationByLessonId(lessonId).orElseGet(() ->{
            PostMeta postMeta = new PostMeta();
            postMeta.setPostId(lessonId);
            postMeta.setMetaKey("_learndash_course_grid_duration");
            postMeta.setMetaValue(duration.toString());
            return postMetaRepository.save(postMeta);
        });

        UserActivityEntity activity = userActivityRepository.findByUserAndLessonAndCourse(userId, lessonId, Long.valueOf(course.getMetaValue())).orElseGet(() -> {
            UserActivityEntity userActivity = new UserActivityEntity();
            userActivity.setUserId(userId);
            userActivity.setPostId(lessonId);
            userActivity.setCourseId(Long.valueOf(course.getMetaValue()));
            userActivity.setActivityType("lesson");
            userActivity.setActivityStatus(0);
            userActivity.setActivityStarted(System.currentTimeMillis() / 1000);
            userActivity.setActivityCompleted(0L);
            userActivity.setActivityUpdated(System.currentTimeMillis() / 1000);
            return userActivityRepository.save(userActivity);
        });

        if(lastSecond > duration) {
            throw new AppException(ErrorCode.LAST_SECOND_EXCEEDS_DURATION);
        }

        activity.setActivityCompleted(lastSecond);
        activity.setActivityStatus((lastSecond >= duration || activity.getActivityStatus() == 1) ? 1 : 0);
        activity.setActivityUpdated(System.currentTimeMillis() / 1000);
        userActivityRepository.save(activity);


        UserCourseEntity userCourse = userCourseRepository.findByUserIdAndCourseId(userId, Long.valueOf(course.getMetaValue())).orElseGet(() -> {
            UserCourseEntity userCourse1 = new UserCourseEntity();
            userCourse1.setUserId(userId);
            userCourse1.setCourseId(Long.valueOf(course.getMetaValue()));
            userCourse1.setCurrentLessonId(0L);
            userCourse1.setProgressPercent(0);
            userCourse1.setStatus("in_progress");
            userCourse1.setStartTime(System.currentTimeMillis() / 1000);
            userCourse1.setIsGradable(false);
            userCourse1.setLngCode("vn");
            return userCourseRepository.save(userCourse1);
        });
        int totalLessons = postMetaRepository.countLessonByCourseId(course.getMetaValue());
        int completedLessons = userActivityRepository.countCompletedLesson(userId, Long.valueOf(course.getMetaValue()));
        int completedPercent = totalLessons == 0 ? 0 : (completedLessons * 100 / totalLessons);

        if(userCourse.getProgressPercent() < 100) {
            userCourse.setProgressPercent(completedPercent);
        }

        if(completedPercent >= 100) {
            userCourse.setStatus("completed");
            userCourse.setEndTime(System.currentTimeMillis() / 1000);
        }
        userCourseRepository.save(userCourse);



        LessonProgressResponse response = new LessonProgressResponse();
        response.setLessonId(lessonId);
        response.setLastPosition(activity.getActivityCompleted());
        response.setCompleted(activity.getActivityStatus() == 1);
        response.setCourseId(Long.valueOf(course.getMetaValue()));
        response.setCompletedPercent(completedPercent);

        return response;
    }

    public LessonProgressResponse getLessonProgress(String email, Long lessonId) {
        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        PostMeta courseMeta = postMetaRepository.findByPostIdAndMetaKey(lessonId, "course_id");
        if(courseMeta == null) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }
        Long courseId = Long.valueOf(courseMeta.getMetaValue());

        UserActivityEntity activity = userActivityRepository
                .findByUserAndLessonAndCourse(userId, lessonId, courseId)
                .orElse(null);


        UserCourseEntity userCourse = userCourseRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);

        LessonProgressResponse response = new LessonProgressResponse();
        response.setLessonId(lessonId);
        response.setCourseId(courseId);
        if(userCourse != null) {
            response.setCompletedPercent(userCourse.getProgressPercent());
        }
        if (activity != null) {
            response.setLastPosition(activity.getActivityCompleted());
            response.setCompleted(activity.getActivityStatus() == 1);

        } else {
            response.setLastPosition(0L);
            response.setCompleted(false);
        }

        return response;
    }

    public CourseProgressResponse getCourseProgress(String email, Long courseId) {
        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        UserCourseEntity userCourse = userCourseRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        List<UserActivityEntity> activities = userActivityRepository.findByUserIdAndCourseId(userId, courseId);

        CourseProgressResponse response = new CourseProgressResponse();
        response.setCourseId(courseId);
        response.setCompletedPercent(userCourse != null ? userCourse.getProgressPercent() : 0);
        response.setLessons(activities.stream().map(a -> {
            LessonProgressResponse lpr = new LessonProgressResponse();
            lpr.setLessonId(a.getPostId());
            lpr.setCompleted(a.getActivityStatus() == 1);
            lpr.setLastPosition(a.getActivityCompleted());
            return lpr;
        }).collect(Collectors.toList()));

        return response;
    }
}
