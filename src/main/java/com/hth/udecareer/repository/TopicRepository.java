package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Topic;
import com.hth.udecareer.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByLessonIdOrderByMenuOrderAsc(Long lessonId);

    List<Topic> findByLessonIdAndStatusOrderByMenuOrderAsc(Long lessonId, PostStatus status);

    Optional<Topic> findByIdAndStatus(Long id, PostStatus status);

    @Query("SELECT t FROM Topic t WHERE t.lessonId = :lessonId AND t.status = 'PUBLISH' ORDER BY t.menuOrder ASC")
    List<Topic> findPublishedByLessonId(@Param("lessonId") Long lessonId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.lessonId = :lessonId AND t.status = 'PUBLISH'")
    long countByLessonId(@Param("lessonId") Long lessonId);

    Page<Topic> findByStatusOrderByDateDesc(PostStatus status, Pageable pageable);
}
