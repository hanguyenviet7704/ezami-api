package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.*;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.FavoritableType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.mapper.CourseMapper;
import com.hth.udecareer.mapper.FavoriteMapper;
import com.hth.udecareer.mapper.LessonMapper;
import com.hth.udecareer.model.request.CourseNoteRequest;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.repository.*;
import com.hth.udecareer.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final PostMetaRepository postMetaRepository;
    private final FavoriteRepository favoriteRepository;
    private final FavoriteMetaRepository favoriteMetaRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserActivityRepository userActivityRepository;

    private final CourseMapper courseMapper;
    private final LessonMapper lessonMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public CourseResponse getCourseInfo(Long courseId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        List<Long> lessonIds = postMetaRepository
                .findLessonIdsByCourseId(String.valueOf(courseId));

        List<Lesson> lessons = lessonRepository.findAllById(lessonIds);

        Map<Long, Lesson> lessonMap = lessons.stream().collect(Collectors.toMap(Lesson::getId, Function.identity()));

        List<Lesson> sortedLessons = lessonIds.stream().map(lessonMap::get).filter(Objects::nonNull).toList();

        List<UserActivityEntity> activityEntities = userActivityRepository.findByUserIdAndCourseId(user.getId(), courseId);


        Map<Long, UserActivityEntity>  activityMap = activityEntities.stream().collect(Collectors.toMap(UserActivityEntity::getPostId, Function.identity(), (oldValue, newValue) -> oldValue));



        CourseResponse courseResponse = courseMapper.toCourseResponse(course);

        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : sortedLessons) {
            LessonResponse lessonResponse = lessonMapper.toLessonResponse(lesson);
           UserActivityEntity userActivityEntity = activityMap.get(lesson.getId());
           if(userActivityEntity == null) {
              lessonResponse.setCompleted(false);
           }else{
               lessonResponse.setCompleted(userActivityEntity.getActivityStatus() == 1);
           }

            lessonResponses.add(lessonResponse);
        }

        courseResponse.setLessons(lessonResponses);

        return courseResponse;
    }

    @Override
    public List<CoursePreResponse> getCourses(Map<String, Object> params) { // without pagnigation
        return courseRepository.findAllCoursesByConditions(params).stream() // using sql native
                .map(courseMapper::toCoursePreResponse).toList();
    }

    @Override
    public Page<CoursePreResponse> getCoursesUsingPagination(Map<String, Object> params, Pageable pageable, @Nullable String email) {
        Page<Course> coursePage = courseRepository.findAllCoursesByConditions(params, pageable);
        return coursePage.map(course -> {
            CoursePreResponse dto = courseMapper.toCoursePreResponse(course);

            if(email != null) {
                Long userId = userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

                UserCourseEntity userCourseEntity = userCourseRepository.findByUserIdAndCourseId(userId, course.getId()).orElse(null);

                dto.setCompletedPercent(userCourseEntity != null && userCourseEntity.getProgressPercent() != null
                        ? userCourseEntity.getProgressPercent()
                        : 0);
            }


            return dto;
        });
    }


    @Override
    @Transactional
    public CourseNoteResponse createOrUpdateCourseNote(Long courseId, CourseNoteRequest request,
                                                       Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        FavoriteEntity favorite = favoriteRepository
                .findByUserIdAndTypeAndFavoritableId(
                        user.getId(),
                        FavoritableType.CNOTE.getValue(), 
                        courseId
                )
                .orElseGet(() -> FavoriteEntity.builder()
                        .userId(user.getId())
                        .favoritableType(FavoritableType.CNOTE)
                        .favoritableId(courseId)
                        .build()
                );

        favorite = favoriteRepository.save(favorite);
        Long favoriteId = favorite.getId();

        FavoriteMetaEntity favoriteMeta = favoriteMetaRepository
                .findByFavoriteIdAndMetaKey(favorite.getId(), FavoritableType.CNOTE.getValue())
                .orElseGet(() -> FavoriteMetaEntity.builder()
                        .favoriteId(favoriteId)
                        .metaKey(FavoritableType.CNOTE.getValue())
                        .build()
                );

        favoriteMeta.setMetaValue(request.getContent());
        favoriteMeta = favoriteMetaRepository.save(favoriteMeta);

        return favoriteMapper.toCourseNoteResponse(favorite, favoriteMeta);
    }

    @Override
    public CourseNoteResponse getCourseNoteResponse(Long courseId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        FavoriteEntity favorite = favoriteRepository
                .findByUserIdAndTypeAndFavoritableId(user.getId(), FavoritableType.CNOTE.getValue(), courseId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Long favoriteId = favorite.getId();

        FavoriteMetaEntity favoriteMeta = favoriteMetaRepository
                .findByFavoriteIdAndMetaKey(favoriteId, FavoritableType.CNOTE.getValue())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return favoriteMapper.toCourseNoteResponse(favorite, favoriteMeta);
    }
}
