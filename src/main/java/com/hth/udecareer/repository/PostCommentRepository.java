package com.hth.udecareer.repository;

import com.hth.udecareer.entities.PostCommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {

    List<PostCommentEntity> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, String status);

    Page<PostCommentEntity> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, String status, Pageable pageable);

    Page<PostCommentEntity> findByPostIdAndStatusOrderByCreatedAtDesc(Long postId, String status, Pageable pageable);

    List<PostCommentEntity> findByPostIdAndParentIdAndStatus(Long postId, Long parentId, String status);

    Page<PostCommentEntity> findByPostIdAndParentIdAndStatusOrderByCreatedAtAsc(Long postId, Long parentId, String status, Pageable pageable);

    Page<PostCommentEntity> findByPostIdAndParentIdAndStatusOrderByCreatedAtDesc(Long postId, Long parentId, String status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM PostCommentEntity c WHERE c.postId = :postId AND c.status = 'published'")
    long countByPostId(@Param("postId") Long postId);

    // Count direct children comments of a parent comment
    @Query("SELECT COUNT(c) FROM PostCommentEntity c WHERE c.parentId = :parentId AND c.status = 'published'")
    long countByParentId(@Param("parentId") Long parentId);

    // Find all direct children of a comment
    List<PostCommentEntity> findByParentId(Long parentId);

    // Find all descendants recursively (using native query for better performance)
    @Query(value = "WITH RECURSIVE comment_tree AS (" +
            "  SELECT id, parent_id, post_id, status FROM wp_fcom_post_comments WHERE parent_id = :parentId " +
            "  UNION ALL " +
            "  SELECT c.id, c.parent_id, c.post_id, c.status FROM wp_fcom_post_comments c " +
            "  INNER JOIN comment_tree ct ON c.parent_id = ct.id" +
            ") SELECT * FROM comment_tree", nativeQuery = true)
    List<PostCommentEntity> findAllDescendants(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM PostCommentEntity c WHERE c.userId = :userId " +
           "AND c.status = 'published' " +
           "AND c.createdAt >= :startOfDay")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
}

