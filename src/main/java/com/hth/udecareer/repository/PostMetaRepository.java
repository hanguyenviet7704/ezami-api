package com.hth.udecareer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.PostMeta;

@Repository
public interface PostMetaRepository extends JpaRepository<PostMeta, Long> {

    List<PostMeta> findAllByPostIdInAndMetaKey(List<Long> postIds, String metaKey);

    PostMeta findByPostIdAndMetaKey(Long postId, String metaKey);
    
    List<PostMeta> findAllByMetaKeyAndMetaValue(String metaKey, String metaValue);

    @Query(value = "SELECT post_id FROM wp_postmeta WHERE meta_key = 'course_id' " +
            "AND meta_value = :courseId", nativeQuery = true)
    List<Long> findLessonIdsByCourseId(@Param("courseId") String courseId);

    boolean existsByPostIdAndMetaKeyAndMetaValue(Long postId, String metaKey, String metaValue);

    @Query("select pm from PostMeta pm where pm.postId = :lessonId and pm.metaKey = '_learndash_course_grid_duration'")
    Optional<PostMeta> findDurationByLessonId(@Param("lessonId") Long lessonId);

    @Query("select count(pm) from PostMeta pm where pm.metaKey = 'course_id' and pm.metaValue = :courseId")
    int countLessonByCourseId(@Param("courseId") String courseId);
}
