package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Post;
import com.hth.udecareer.entities.TermEntity;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.model.projection.PostDataProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    List<Post> findAllByStatusAndType(PostStatus status, String type);

    /**
     * Override findById với EAGER fetch cho postMetas và authorUser
     */
    @Query("SELECT pt FROM Post pt " +
            "LEFT JOIN FETCH pt.postMetas " +
            "LEFT JOIN FETCH pt.authorUser " +
            "WHERE pt.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT pt FROM Post pt, TermRelationshipEntity trp, TermTaxonomyEntity tty " +
            "WHERE pt.id = trp.id.objectId AND trp.id.termTaxonomyId = tty.id " +
            "AND tty.taxonomy = 'category' " +
            "AND tty.termId = :categoryId " +
            "AND pt.status in :postStatues " +
            "ORDER BY pt.date")
    List<Post> findByCategoryId(@Param("categoryId") Long categoryId,
                                @Param("postStatues") List<PostStatus> postStatuses);

    @Query("SELECT tm FROM TermEntity tm, TermTaxonomyEntity tty " +
            "WHERE tm.id = tty.termId " +
            "AND tty.taxonomy = 'category' " +
            "AND tm.slug in :slugs")
    List<TermEntity> findCategoryBySlug(@Param("slugs") Collection<String> slugs);


    @Query("""
                SELECT tm, COUNT(child)
                FROM TermEntity tm
                JOIN TermTaxonomyEntity tty ON tm.id = tty.termId
                LEFT JOIN TermTaxonomyEntity child ON child.parent = tty.termId
                WHERE tty.taxonomy = 'category' AND tty.parent = 0
                GROUP BY tm, tty.termId
                ORDER BY tm.id
            """)
    List<Object[]> findAllCategoriesRootWithChildCount();


    @Query("""
                SELECT tm, COUNT(child)
                FROM TermEntity tm
                JOIN TermTaxonomyEntity tty ON tm.id = tty.termId
                LEFT JOIN TermTaxonomyEntity child ON child.parent = tty.termId
                WHERE tty.taxonomy = 'category' AND tty.parent = :parentId
                GROUP BY tm, tty.termId
                ORDER BY tm.id
            """)
    List<Object[]> findAllCategoriesByParent(@Param("parentId") Long parentId);

    @Query("""
        SELECT tm, tty.parent
        FROM TermEntity tm
        JOIN TermTaxonomyEntity tty ON tm.id = tty.termId
        WHERE tty.taxonomy = 'category'
    """)
    List<Object[]> findAllCategories();

    @Query("""
        SELECT tm, tty.parent, tty.count, tty.description
        FROM TermEntity tm
        JOIN TermTaxonomyEntity tty ON tm.id = tty.termId
        WHERE tty.taxonomy = 'category'
    """)
    List<Object[]> findAllCategoriesWithCount();


    /**
     * Tìm post theo slug (SEO-friendly URL)
     * Ví dụ: /post/slug/huong-dan-wordpress
     */
    @Query("SELECT pt FROM Post pt " +
            "LEFT JOIN FETCH pt.postMetas " +
            "LEFT JOIN FETCH pt.authorUser " +
            "WHERE pt.name = :slug " +
            "AND pt.type = 'post' " +
            "AND pt.status IN :statuses")
    Optional<Post> findBySlugWithDetails(
            @Param("slug") String slug,
            @Param("statuses") List<PostStatus> statuses
    );

    /**
     * Tìm post theo slug (SEO-friendly URL) - method cũ không fetch
     */
    @Query("SELECT pt FROM Post pt " +
            "WHERE pt.name = :slug " +
            "AND pt.type = 'post' " +
            "AND pt.status IN :statuses")
    Optional<Post> findBySlug(
            @Param("slug") String slug,
            @Param("statuses") List<PostStatus> statuses
    );

    /**
     * Tìm attachment (image) theo ID để lấy URL và title
     */
    @Query("SELECT pt FROM Post pt WHERE pt.id = :attachmentId AND pt.type = 'attachment'")
    Optional<Post> findAttachmentById(@Param("attachmentId") Long attachmentId);

    // ============= PAGINATION METHODS =============

    /**
     * Tìm tất cả posts theo type và status với pagination
     */
    Page<Post> findAllByTypeAndStatusIn(String type, List<PostStatus> statuses, Pageable pageable);

    /**
     * Tìm posts theo category với pagination
     */
    @Query("SELECT DISTINCT pt FROM Post pt " +
            "JOIN TermRelationshipEntity trp ON pt.id = trp.id.objectId " +
            "JOIN TermTaxonomyEntity tty ON trp.id.termTaxonomyId = tty.id " +
            "WHERE tty.taxonomy = 'category' " +
            "AND tty.termId = :categoryId " +
            "AND pt.type = :postType " +
            "AND pt.status IN :statuses")
    Page<Post> findByCategoryIdAndType(
            @Param("categoryId") Long categoryId,
            @Param("postType") String postType,
            @Param("statuses") List<PostStatus> statuses,
            Pageable pageable
    );

    /**
     * Tìm kiếm posts theo keyword trong title hoặc content
     */
    @Query("SELECT pt FROM Post pt " +
            "WHERE pt.type = :postType " +
            "AND pt.status IN :statuses " +
            "AND (LOWER(pt.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(pt.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("postType") String postType,
            @Param("statuses") List<PostStatus> statuses,
            Pageable pageable
    );

    /**
     * Tìm posts theo author username
     */
    @Query("SELECT pt FROM Post pt " +
            "JOIN User u ON pt.author = u.id " +
            "WHERE pt.type = :postType " +
            "AND pt.status IN :statuses " +
            "AND u.username = :authorUsername")
    Page<Post> findByAuthorUsername(
            @Param("authorUsername") String authorUsername,
            @Param("postType") String postType,
            @Param("statuses") List<PostStatus> statuses,
            Pageable pageable
    );

    /**
     * Tìm posts theo author ID
     */
    @Query("SELECT pt FROM Post pt " +
            "WHERE pt.type = :postType " +
            "AND pt.status IN :statuses " +
            "AND pt.author = :authorId")
    Page<Post> findByAuthorId(
            @Param("authorId") Long authorId,
            @Param("postType") String postType,
            @Param("statuses") List<PostStatus> statuses,
            Pageable pageable
    );

    /**
     * Lấy post data với quiz category cho favorites
     * Chỉ lấy posts có status = 'publish' hoặc 'private'
     */
    @Query(value = """
            SELECT 
                p.ID as postId,
                p.post_title as postTitle,
                p.post_status as postStatus,
                qc.code as categoryCode,
                qc.title as categoryTitle,
                qc.image_uri as categoryImageUri
            FROM wp_posts p
            LEFT JOIN ez_quiz_category qc ON p.post_title LIKE CONCAT(qc.title, '%')
            WHERE p.ID IN :postIds
            AND p.post_status IN ('publish', 'private')
            """, nativeQuery = true)
    List<PostDataProjection> findPostDataByIds(@Param("postIds") List<Long> postIds);

    /**
     * Lấy post_type theo ID để add favorite
     * Chỉ lấy posts có status = 'publish' hoặc 'private'
     */
    @Query("SELECT p.type FROM Post p WHERE p.id = :postId AND p.status IN :statuses")
    Optional<String> findPostTypeById(
            @Param("postId") Long postId,
            @Param("statuses") List<PostStatus> statuses
    );
}

