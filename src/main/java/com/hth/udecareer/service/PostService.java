package com.hth.udecareer.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hth.udecareer.entities.ArticleSpaceCategoryEntity;
import com.hth.udecareer.entities.ArticleSpaceEntity;
import com.hth.udecareer.entities.Post;
import com.hth.udecareer.entities.TermEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.AttachmentInfo;
import com.hth.udecareer.model.request.PostFilterRequest;
import com.hth.udecareer.model.response.ArticleSpaceResponse;
import com.hth.udecareer.model.response.PostResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.ArticleSpaceCategoryRepository;
import com.hth.udecareer.repository.ArticleSpaceRepository;
import com.hth.udecareer.repository.PostMetaRepository;
import com.hth.udecareer.repository.PostRepository;
import com.hth.udecareer.specification.PostSpecification;
import com.hth.udecareer.utils.PostImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ArticleSpaceRepository articleSpaceRepository;
    private final ArticleSpaceCategoryRepository articleSpaceCategoryRepository;
    private final PostMetaRepository postMetaRepository;

    // ============= PRIVATE HELPER METHODS =============

    /**
     * Get attachment info from Post entity
     *
     * @param post Post entity with fetched postMetas
     * @return AttachmentInfo or null if not found
     */
    private AttachmentInfo getAttachmentInfo(Post post) {
        if (post == null) {
            return null;
        }

        try {
            Long thumbnailId = PostImageUtil.getThumbnailId(post.getPostMetas());
            if (thumbnailId == null) {
                return null;
            }

            return postRepository.findAttachmentById(thumbnailId)
                    .map(this::buildAttachmentInfo)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to get attachment info for post {}: {}", post.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Build AttachmentInfo from Post entity
     */
    private AttachmentInfo buildAttachmentInfo(Post attachment) {
        return AttachmentInfo.builder()
                .id(attachment.getId())
                .url(attachment.getGuid())
                .title(attachment.getTitle())
                .build();
    }

    /**
     * Convert Post entity to PostResponse with attachment info
     *
     * @param post Post entity
     * @param showContent true = show full content, false = empty content
     * @return PostResponse
     */
    private PostResponse toPostResponse(Post post, boolean showContent) {
        AttachmentInfo attachmentInfo = getAttachmentInfo(post);
        return PostResponse.from(post, showContent, attachmentInfo);
    }

    /**
     * Batch convert List<Post> to List<PostResponse> with optimized attachment fetching
     * Reduces N+1 queries to 2-3 queries total
     *
     * @param posts List of Post entities
     * @param showContent true = show full content, false = empty content
     * @return List of PostResponse with attachments
     */
    private List<PostResponse> toPostResponseListOptimized(List<Post> posts, boolean showContent) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        Map<Long, AttachmentInfo> attachmentMap = getAttachmentInfoBatch(postIds);

        return posts.stream()
                .map(post -> PostResponse.from(
                        post,
                        showContent,
                        attachmentMap.get(post.getId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Batch fetch attachment info for multiple posts
     * Optimized: 2 queries instead of N queries
     *
     * Query flow:
     * 1. SELECT * FROM wp_postmeta WHERE post_id IN (...) AND meta_key = '_thumbnail_id'
     * 2. SELECT * FROM wp_posts WHERE id IN (...) AND post_type = 'attachment'
     *
     * @param postIds List of post IDs
     * @return Map of postId -> AttachmentInfo
     */
    private Map<Long, AttachmentInfo> getAttachmentInfoBatch(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        try {
            // Query 1: Fetch all thumbnail metas
            List<com.hth.udecareer.entities.PostMeta> thumbnailMetas =
                    postMetaRepository.findAllByPostIdInAndMetaKey(postIds, "_thumbnail_id");

            if (thumbnailMetas.isEmpty()) {
                return Map.of();
            }

            // Parse attachment IDs from meta values
            List<Long> attachmentIds = extractAttachmentIds(thumbnailMetas);
            if (attachmentIds.isEmpty()) {
                return Map.of();
            }

            // Query 2: Fetch all attachment posts
            Map<Long, AttachmentInfo> attachmentInfoMap = fetchAttachmentInfoMap(attachmentIds);

            // Build final map: postId -> AttachmentInfo
            return buildPostToAttachmentMap(thumbnailMetas, attachmentInfoMap);

        } catch (Exception e) {
            log.error("Failed to batch fetch attachments: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * Extract attachment IDs from thumbnail meta values
     */
    private List<Long> extractAttachmentIds(List<com.hth.udecareer.entities.PostMeta> thumbnailMetas) {
        return thumbnailMetas.stream()
                .map(com.hth.udecareer.entities.PostMeta::getMetaValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(this::parseAttachmentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Safely parse attachment ID from string
     */
    private Long parseAttachmentId(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid attachment ID value: {}", value);
            return null;
        }
    }

    /**
     * Fetch and build attachment info map
     */
    private Map<Long, AttachmentInfo> fetchAttachmentInfoMap(List<Long> attachmentIds) {
        List<Post> attachments = postRepository.findAllById(attachmentIds);

        return attachments.stream()
                .filter(att -> "attachment".equals(att.getType()))
                .collect(Collectors.toMap(
                        Post::getId,
                        this::buildAttachmentInfo,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Build final map: postId -> AttachmentInfo
     */
    private Map<Long, AttachmentInfo> buildPostToAttachmentMap(
            List<com.hth.udecareer.entities.PostMeta> thumbnailMetas,
            Map<Long, AttachmentInfo> attachmentInfoMap) {

        return thumbnailMetas.stream()
                .collect(Collectors.toMap(
                        com.hth.udecareer.entities.PostMeta::getPostId,
                        meta -> {
                            Long attachmentId = parseAttachmentId(meta.getMetaValue());
                            return attachmentId != null ? attachmentInfoMap.get(attachmentId) : null;
                        },
                        (existing, replacement) -> existing,
                        HashMap::new
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // ============= ARTICLE SPACE METHODS =============

    @Cacheable(value = "article_spaces", key = "#appCode + ':' + #language + ':' + #withCategory")
    public List<ArticleSpaceResponse> findAllSpace(@NotNull String appCode,
                                                   @NotNull String language,
                                                   boolean withCategory) {
        final List<ArticleSpaceEntity> articleSpaceEntityList = articleSpaceRepository.findAllEnableByAppCode(appCode);

        final List<ArticleSpaceResponse> articleSpaceResponses;
        if (withCategory) {
            final List<ArticleSpaceCategoryEntity> articleSpaceCategoryEntities =
                    articleSpaceCategoryRepository.findAllEnableByLanguage(language);
            final Multimap<Long, ArticleSpaceCategoryEntity> spaceToCategory = ArrayListMultimap.create();
            articleSpaceCategoryEntities.forEach(x -> spaceToCategory.put(x.getSpace_id(), x));

            final Map<String, TermEntity> categoryMap = getCategoryMap(articleSpaceCategoryEntities);

            articleSpaceResponses = articleSpaceEntityList
                    .stream()
                    .map(space -> {
                        final Collection<ArticleSpaceCategoryEntity> categoryEntities = spaceToCategory.get(
                                space.getId());

                        return getSpaceResponse(space, categoryEntities, categoryMap);
                    })
                    .filter(x -> !CollectionUtils.isEmpty(x.getCategories()))
                    .collect(Collectors.toList());
        } else {
            articleSpaceResponses = articleSpaceEntityList
                    .stream()
                    .map(space -> ArticleSpaceResponse.from(space.getId(), space.getTitle()))
                    .collect(Collectors.toList());
        }

        return articleSpaceResponses;
    }

    private Map<String, TermEntity> getCategoryMap(
            final List<ArticleSpaceCategoryEntity> articleSpaceCategoryEntities) {
        final Set<String> categorySlugs =
                articleSpaceCategoryEntities.stream()
                        .map(ArticleSpaceCategoryEntity::getCategorySlug)
                        .collect(Collectors.toSet());

        final List<TermEntity> categories = postRepository.findCategoryBySlug(categorySlugs);
        return categories.stream().collect(Collectors.toMap(TermEntity::getSlug, Function.identity()));
    }

    private static ArticleSpaceResponse getSpaceResponse(
            @NotNull final ArticleSpaceEntity space,
            @Nullable final Collection<ArticleSpaceCategoryEntity> categoryEntities,
            @NotNull final Map<String, TermEntity> categoryMap) {
        if (CollectionUtils.isEmpty(categoryEntities)) {
            return ArticleSpaceResponse.from(space.getId(), space.getTitle(), List.of());
        }

        final List<ArticleSpaceResponse.Category> categoriesList =
                categoryEntities
                        .stream()
                        .filter(x -> categoryMap.containsKey(x.getCategorySlug()))
                        .map(x -> {
                            final TermEntity termEntity = categoryMap.get(x.getCategorySlug());
                            final String categoryName =
                                    StringUtils.isBlank(x.getCategoryName()) ?
                                            termEntity.getName().trim() : x.getCategoryName().trim();
                            return ArticleSpaceResponse.Category.from(termEntity.getId(), categoryName);
                        })
                        .filter(Objects::nonNull)
                        .toList();

        return ArticleSpaceResponse.from(space.getId(), space.getTitle(), categoriesList);
    }

    //@Cacheable(value = "post_spaces", key = "'space_' + #spaceId + '_' + #language")
    public ArticleSpaceResponse getSpace(Long spaceId,
                                         @NotNull String language) throws AppException {
        final ArticleSpaceEntity articleSpaceEntity =
                articleSpaceRepository.findById(spaceId)
                        .orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        final List<ArticleSpaceCategoryEntity> articleSpaceCategoryEntities =
                articleSpaceCategoryRepository.findAllEnableBySpaceAndLanguage(spaceId, language);

        final Map<String, TermEntity> categoryMap = getCategoryMap(articleSpaceCategoryEntities);

        return getSpaceResponse(articleSpaceEntity, articleSpaceCategoryEntities, categoryMap);
    }

    //@Cacheable(value = "posts", key = "(#categoryId != null ? 'category_' + #categoryId : 'all_posts')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponse> findAllByCategory(@NotNull Long categoryId) {
        List<Post> postList = postRepository.findByCategoryId(
                categoryId,
                List.of(PostStatus.PUBLISH, PostStatus.FUTURE, PostStatus.PRIVATE)
        );

        return toPostResponseListOptimized(postList, false);
    }

    //@Cacheable(value = "post_detail", key = "#postId")
    public PostResponse findById(@NotNull Long postId) throws AppException {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        return toPostResponse(post, true);
    }

    /**
     * Find post by slug (SEO-friendly URL)
     * Example: /post/slug/huong-dan-wordpress
     */
    @Cacheable(value = "post_detail", key = "'slug_' + #slug")
    public PostResponse findBySlug(@NotNull String slug) throws AppException {
        List<PostStatus> publishedStatuses = List.of(
                PostStatus.PUBLISH,
                PostStatus.FUTURE,
                PostStatus.PRIVATE
        );

        Post post = postRepository.findBySlugWithDetails(slug, publishedStatuses)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        return toPostResponse(post, true);
    }

    /**
     * Search and filter posts with pagination (OPTIMIZED)
     * Combines multiple filters: keyword + categoryIds + authorId + tags + dateRange
     * Uses batch processing to reduce N+1 queries
     *
     * @param filter Filter parameters
     * @param pageable Pagination parameters
     * @return PageResponse with posts and metadata
     * @throws AppException POST_NOT_FOUND if no posts found
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<PostResponse> searchAndFilterPosts(
            @NotNull PostFilterRequest filter,
            @NotNull Pageable pageable) throws AppException {

        log.debug("Searching posts with filter: {} and pagination: {}", filter, pageable);

        filter.validate();

        Specification<Post> spec = PostSpecification.buildSpecification(filter);
        Page<Post> postPage = postRepository.findAll(spec, pageable);

        if (postPage.isEmpty()) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        List<PostResponse> responses = toPostResponseListOptimized(postPage.getContent(), false);

        return PageResponse.<PostResponse>builder()
                .content(responses)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();
    }

    /**
     * Search and filter posts without pagination (OPTIMIZED)
     * Returns all matching posts as a list
     *
     * @param filter Filter parameters
     * @return List of PostResponse
     * @throws AppException POST_NOT_FOUND if no posts found
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponse> searchAndFilterPostsAsList(@NotNull PostFilterRequest filter) throws AppException {

        log.debug("Searching posts without pagination - Filter: {}", filter);

        filter.validate();

        Specification<Post> spec = PostSpecification.buildSpecification(filter);
        List<Post> postList = postRepository.findAll(spec);

        if (postList.isEmpty()) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        return toPostResponseListOptimized(postList, false);
    }

    // ============= OLD PAGINATION METHODS (DEPRECATED - kept for backward compatibility) =============

    /**
     * @deprecated Use {@link #searchAndFilterPosts(PostFilterRequest, Pageable)} instead
     */
    @Deprecated
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<PostResponse> findAllPosts(@Nullable Long categoryId, @NotNull Pageable pageable) {
        final List<PostStatus> publishedStatuses = List.of(
                PostStatus.PUBLISH,
                PostStatus.FUTURE,
                PostStatus.PRIVATE
        );

        final Page<Post> postPage;
        if (categoryId != null) {
            postPage = postRepository.findByCategoryIdAndType(
                    categoryId,
                    "post",
                    publishedStatuses,
                    pageable
            );
        } else {
            postPage = postRepository.findAllByTypeAndStatusIn(
                    "post",
                    publishedStatuses,
                    pageable
            );
        }

        return PageResponse.of(postPage.map(post -> toPostResponse(post, false)));
    }

    /**
     * @deprecated Use {@link #searchAndFilterPosts(PostFilterRequest, Pageable)} instead
     */
    @Deprecated
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<PostResponse> searchPosts(@NotNull String keyword, @NotNull Pageable pageable) {
        final List<PostStatus> publishedStatuses = List.of(
                PostStatus.PUBLISH,
                PostStatus.FUTURE,
                PostStatus.PRIVATE
        );

        final Page<Post> postPage = postRepository.searchByKeyword(
                keyword.trim(),
                "post",
                publishedStatuses,
                pageable
        );

        return PageResponse.of(postPage.map(post -> toPostResponse(post, false)));
    }

    /**
     * @deprecated Use {@link #searchAndFilterPosts(PostFilterRequest, Pageable)} instead
     */
    @Deprecated
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<PostResponse> findPostsByAuthor(@NotNull String authorUsername, @NotNull Pageable pageable) {
        final List<PostStatus> publishedStatuses = List.of(
                PostStatus.PUBLISH,
                PostStatus.FUTURE,
                PostStatus.PRIVATE
        );

        final Page<Post> postPage = postRepository.findByAuthorUsername(
                authorUsername.trim(),
                "post",
                publishedStatuses,
                pageable
        );

        return PageResponse.of(postPage.map(post -> toPostResponse(post, false)));
    }

    /**
     * @deprecated Use {@link #searchAndFilterPosts(PostFilterRequest, Pageable)} instead
     */
    @Deprecated
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<PostResponse> findPostsByAuthorId(@NotNull Long authorId, @NotNull Pageable pageable) {
        final List<PostStatus> publishedStatuses = List.of(
                PostStatus.PUBLISH,
                PostStatus.FUTURE,
                PostStatus.PRIVATE
        );

        final Page<Post> postPage = postRepository.findByAuthorId(
                authorId,
                "post",
                publishedStatuses,
                pageable
        );

        return PageResponse.of(postPage.map(post -> toPostResponse(post, false)));
    }
}
