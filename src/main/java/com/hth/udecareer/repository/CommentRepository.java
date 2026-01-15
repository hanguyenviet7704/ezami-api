package com.hth.udecareer.repository;

import com.hth.udecareer.entities.CommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Query("SELECT c FROM CommentEntity c " +
            "WHERE c.commentPostId = :postId " +
            "AND c.commentParent = 0 " +
            "AND c.commentApproved = '1'" +
            "ORDER BY c.commentDate DESC ")
    List<CommentEntity> findCommentByCommentPostIdRoot(Long postId, Pageable pageable);


    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.commentPostId = :postId AND c.commentApproved = '1'")
    long countAllCommentsByPostId(Long postId);


    @Query("SELECT c FROM CommentEntity c " +
            "WHERE c.commentParent = :parentId " +
            "AND c.commentApproved = '1'" +
            "ORDER BY c.commentDate DESC ")
    List<CommentEntity> findCommentByCommentParent(Long parentId,  Pageable pageable);

    Optional<CommentEntity> findCommentByCommentId(Long commentId);


    @Modifying
    @Query(value = "UPDATE wp_commentmeta " +
            "SET meta_value = :count " +
            "WHERE comment_id = :commentId AND meta_key = 'total_replies'", nativeQuery = true)
    void updateTotalReplies(Long commentId, Long count);

    @Modifying
    @Query(value = "INSERT INTO wp_commentmeta (comment_id, meta_key, meta_value) " +
            "SELECT :commentId, 'total_replies', :count " +
            "WHERE NOT EXISTS (" +
            "    SELECT 1 FROM wp_commentmeta " +
            "    WHERE comment_id = :commentId AND meta_key = 'total_replies'" +
            ")", nativeQuery = true)
    void insertTotalRepliesIfNotExist(Long commentId, Long count);




    @Query(value = "SELECT CAST(meta_value AS UNSIGNED) FROM wp_commentmeta " +
            "WHERE comment_id = :commentId AND meta_key = 'total_replies'", nativeQuery = true)
    Long getTotalReplies(Long commentId);

    @Query(value = "SELECT c FROM CommentEntity c WHERE c.commentParent = :parentId")
    List<CommentEntity> findChildren(Long parentId);


    @Modifying
    @Query(value = "DELETE FROM wp_commentmeta WHERE comment_id IN :commentIds", nativeQuery = true)
    void deleteMetaByCommentIds(List<Long> commentIds);


    @Modifying
    @Query(value = "DELETE FROM CommentEntity c WHERE c.commentId IN :ids")
    void deleteCommentsByIds(List<Long> ids);
}
