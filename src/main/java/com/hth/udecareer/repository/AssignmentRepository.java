package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Assignment;
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
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByAuthorIdOrderByDateDesc(Long authorId);

    Page<Assignment> findByAuthorIdOrderByDateDesc(Long authorId, Pageable pageable);

    List<Assignment> findByParentIdOrderByDateDesc(Long parentId);

    Page<Assignment> findByParentIdOrderByDateDesc(Long parentId, Pageable pageable);

    Optional<Assignment> findByIdAndAuthorId(Long id, Long authorId);

    @Query("SELECT a FROM Assignment a WHERE a.authorId = :userId AND a.status = :status ORDER BY a.date DESC")
    List<Assignment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PostStatus status);

    @Query("SELECT a FROM Assignment a WHERE a.parentId = :lessonId AND a.status = :status ORDER BY a.date DESC")
    List<Assignment> findByLessonIdAndStatus(@Param("lessonId") Long lessonId, @Param("status") PostStatus status);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.authorId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.parentId = :lessonId")
    long countByLessonId(@Param("lessonId") Long lessonId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.authorId = :userId AND a.status = 'GRADED'")
    long countGradedByUserId(@Param("userId") Long userId);
}
