package com.hth.udecareer.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.UserActivityEntity;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivityEntity, Long> {

    @Query("SELECT uae FROM UserActivityEntity uae "
           + "WHERE uae.id IN "
           + "(SELECT MAX(uay.id) "
           + "FROM UserActivityEntity uay "
           + "WHERE uay.userId = :userId AND uay.activityType = 'quiz' "
           + "GROUP BY uay.postId)")
    List<UserActivityEntity> getLatestActivityByUserId(@Param("userId") Long userId);

    @Query("SELECT uae FROM UserActivityEntity uae "
           + "WHERE uae.id IN "
           + "(SELECT MAX(uay.id) "
           + "FROM UserActivityEntity uay "
           + "WHERE uay.userId = :userId AND uay.activityType = 'quiz' "
           + "AND uay.postId IN :postIds "
           + "GROUP BY uay.postId)")
    List<UserActivityEntity> getLatestActivityByUserIdAndPostIds(@Param("userId") Long userId,
                                                                 @Param("postIds") List<Long> postIds);

    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId AND ua.postId = :postId " +
            "AND (ua.activityCompleted IS NULL OR ua.activityCompleted = 0) " +
            "ORDER BY ua.id DESC")
    List<UserActivityEntity> findLatestUncompletedActivity(@Param("userId") Long userId,
                                                           @Param("postId") Long postId,
                                                           Pageable pageable);
    default Optional<UserActivityEntity> findLatestUncompletedActivity(Long userId, Long postId) {
        return findLatestUncompletedActivity(userId, postId, PageRequest.of(0, 1)).stream().findFirst();
    }

    // Tìm draft theo metadata is_draft = "1"
    @Query("SELECT ua FROM UserActivityEntity ua " +
           "JOIN UserActivityMetaEntity meta ON ua.id = meta.activityId " +
           "WHERE ua.userId = :userId AND ua.postId = :postId " +
           "AND meta.activityMetaKey = 'is_draft' AND meta.activityMetaValue = '1' " +
           "ORDER BY ua.id DESC")
    List<UserActivityEntity> findLatestDraftByMetadata(@Param("userId") Long userId,
                                                       @Param("postId") Long postId,
                                                       Pageable pageable);

    default Optional<UserActivityEntity> findLatestDraftByMetadata(Long userId, Long postId) {
        return findLatestDraftByMetadata(userId, postId, PageRequest.of(0, 1)).stream().findFirst();
    }

    // Lấy tất cả activities cho các postIds (không chỉ mới nhất)
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId AND ua.activityType = 'quiz' " +
            "AND ua.postId IN :postIds " +
            "ORDER BY ua.postId, ua.id DESC")
    List<UserActivityEntity> findAllActivitiesByUserIdAndPostIds(@Param("userId") Long userId,
                                                                  @Param("postIds") List<Long> postIds);

    // Lấy lịch sử làm bài của user (chỉ những bài đã hoàn thành, không lấy draft)
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.activityType = 'quiz' " +
            "AND ua.activityCompleted IS NOT NULL " +
            "AND ua.activityCompleted > 0 " +
            "ORDER BY ua.activityCompleted DESC")
    org.springframework.data.domain.Page<UserActivityEntity> findCompletedQuizHistory(
            @Param("userId") Long userId,
            Pageable pageable);

    // Lấy lịch sử làm bài của user theo quizId cụ thể
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.activityType = 'quiz' " +
            "AND ua.postId = :postId " +
            "AND ua.activityCompleted IS NOT NULL " +
            "AND ua.activityCompleted > 0 " +
            "ORDER BY ua.activityCompleted DESC")
    org.springframework.data.domain.Page<UserActivityEntity> findCompletedQuizHistoryByPostId(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            Pageable pageable);

    // Lấy lịch sử làm bài với filter theo khoảng thời gian
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.activityType = 'quiz' " +
            "AND ua.activityCompleted IS NOT NULL " +
            "AND ua.activityCompleted > 0 " +
            "AND (:fromDate IS NULL OR ua.activityCompleted >= :fromDate) " +
            "AND (:toDate IS NULL OR ua.activityCompleted <= :toDate)")
    org.springframework.data.domain.Page<UserActivityEntity> findCompletedQuizHistoryByDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") Long fromDate,
            @Param("toDate") Long toDate,
            Pageable pageable);

    // Lấy lịch sử làm bài theo quizId và khoảng thời gian
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.activityType = 'quiz' " +
            "AND ua.postId = :postId " +
            "AND ua.activityCompleted IS NOT NULL " +
            "AND ua.activityCompleted > 0 " +
            "AND (:fromDate IS NULL OR ua.activityCompleted >= :fromDate) " +
            "AND (:toDate IS NULL OR ua.activityCompleted <= :toDate)")
    org.springframework.data.domain.Page<UserActivityEntity> findCompletedQuizHistoryByPostIdAndDateRange(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            @Param("fromDate") Long fromDate,
            @Param("toDate") Long toDate,
            Pageable pageable);

    // Tìm activity theo user, lesson và course
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.postId = :lessonId " +
            "AND ua.courseId = :courseId " +
            "ORDER BY ua.id DESC")
    List<UserActivityEntity> findByUserAndLessonAndCourseList(
            @Param("userId") Long userId,
            @Param("lessonId") Long lessonId,
            @Param("courseId") Long courseId,
            Pageable pageable);

    default Optional<UserActivityEntity> findByUserAndLessonAndCourse(Long userId, Long lessonId, Long courseId) {
        return findByUserAndLessonAndCourseList(userId, lessonId, courseId, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    // Đếm số lesson đã hoàn thành của user trong course
    @Query("SELECT COUNT(ua) FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.courseId = :courseId " +
            "AND ua.activityType = 'lesson' " +
            "AND ua.activityStatus = 1")
    int countCompletedLesson(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId);

    // Tìm tất cả activities theo userId và courseId
    @Query("SELECT ua FROM UserActivityEntity ua " +
            "WHERE ua.userId = :userId " +
            "AND ua.courseId = :courseId " +
            "ORDER BY ua.id DESC")
    List<UserActivityEntity> findByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId);
}
