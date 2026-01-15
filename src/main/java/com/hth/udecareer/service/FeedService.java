package com.hth.udecareer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.entities.*;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.NotificationType;
import com.hth.udecareer.enums.SourceObjectType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.AffiliateRepository;
import com.hth.udecareer.model.request.*;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.repository.*;
import com.hth.udecareer.utils.HtmlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private static final int DAILY_POST_LIMIT = 100000000;
    private static final int DAILY_COMMENT_LIMIT = 100000000;
    private static final int DAILY_REACTION_LIMIT = 100000000;

    private final FeedPostRepository feedPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostReactionRepository postReactionRepository;
    private final XProfileRepository xProfileRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceUserRepository spaceUserRepository;
    private final MediaArchiveRepository mediaArchiveRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final UserPointsService userPointsService;
    private final AffiliateRepository affiliateRepository;

    @Value("${app.domain}")
    private String appDomain;


    // ============= LIST FEEDS =============

    @Transactional(readOnly = true)
    public FeedListResponse listFeeds(FeedListRequest request, Long currentUserId) {
        long startTime = System.currentTimeMillis();

        // Pagination - all optional with defaults
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int perPage = request.getPerPage() != null && request.getPerPage() > 0 ? request.getPerPage() : 10;

        // Handle order_by_type if provided
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Default: latest first
        if (StringUtils.isNotBlank(request.getOrderByType())) {
            String orderType = request.getOrderByType().toLowerCase().trim();
            switch (orderType) {
                case "latest":
                case "created_at_desc":
                case "newest":
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
                case "oldest":
                case "created_at_asc":
                    sort = Sort.by(Sort.Direction.ASC, "createdAt");
                    break;
                case "most_commented":
                case "comments_desc":
                    sort = Sort.by(Sort.Direction.DESC, "commentsCount");
                    break;
                case "most_reactions":
                case "reactions_desc":
                case "most_liked":
                    sort = Sort.by(Sort.Direction.DESC, "reactionsCount");
                    break;
                case "trending":
                    // Trending: combination of reactions and comments, sorted by created_at desc
                    // For now, use reactions_count desc as trending indicator
                    sort = Sort.by(Sort.Direction.DESC, "reactionsCount", "createdAt");
                    break;
                default:
                    // Keep default sorting if unknown type
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, perPage, sort);

        // Resolve space ID if space slug provided (optional)
        Long spaceId = null;
        if (StringUtils.isNotBlank(request.getSpace())) {
            Optional<SpaceEntity> spaceOpt = spaceRepository.findBySlug(request.getSpace());
            if (spaceOpt.isPresent()) {
                spaceId = spaceOpt.get().getId();
            }
        }

        // Use unified query that handles all optional filters
        // This allows combining multiple filters: space + user + search
        Page<FeedPostEntity> feedPage = feedPostRepository.findFeedsWithFilters(
                spaceId,           // optional: filter by space
                request.getUserId(), // optional: filter by user
                StringUtils.isNotBlank(request.getSearch()) ? request.getSearch() : null, // optional: search keyword
                null,              // topic filter removed
                pageable
        );


        // Convert to response
        List<FeedResponse> feedResponses = feedPage.getContent().stream()
                .map(feed -> convertToFeedResponse(feed, currentUserId))
                .collect(Collectors.toList());

        // Build PageResponse using the standard format
        PageResponse<FeedResponse> feedsPageResponse = PageResponse.of(feedResponses, pageable, feedPage.getTotalElements());

        long executionTime = System.currentTimeMillis() - startTime;

        return FeedListResponse.builder()
                .feeds(feedsPageResponse)
                .lastFetchedTimestamp(page == 0 ? System.currentTimeMillis() / 1000 : null)
                .executionTime(executionTime / 1000.0)
                .build();
    }

    // ============= GET FEED BY SLUG =============

    @Transactional(readOnly = true)
    public FeedDetailResponse getFeedBySlug(String slug, String context, Long currentUserId) {
        long startTime = System.currentTimeMillis();

        FeedPostEntity feed = feedPostRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feed not found"));

        // Check if feed is published (not deleted) - unless context is "edit"
        if (!"edit".equals(context) && !"published".equals(feed.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Feed not found");
        }

        FeedResponse feedResponse = convertToFeedResponse(feed, currentUserId);
        long executionTime = System.currentTimeMillis() - startTime;

        return FeedDetailResponse.builder()
                .feed(feedResponse)
                .executionTime(executionTime / 1000.0)
                .build();
    }

    // ============= GET FEED BY ID =============

    @Transactional(readOnly = true)
    public FeedDetailResponse getFeedById(Long feedId, Long currentUserId) {
        long startTime = System.currentTimeMillis();

        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feed not found"));

        // Check if feed is published (not deleted)
        if (!"published".equals(feed.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Feed not found");
        }

        FeedResponse feedResponse = convertToFeedResponse(feed, currentUserId);
        long executionTime = System.currentTimeMillis() - startTime;

        return FeedDetailResponse.builder()
                .feed(feedResponse)
                .executionTime(executionTime / 1000.0)
                .build();
    }

    // ============= CREATE FEED =============

    @Transactional
    public FeedCreateResponse createFeed(FeedCreateRequest request, Long userId) {
        // Check daily post limit
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayPostCount = feedPostRepository.countByUserIdAndCreatedAtAfter(userId, startOfDay);
        if (todayPostCount >= DAILY_POST_LIMIT) {
            throw new AppException(ErrorCode.DAILY_POST_LIMIT_EXCEEDED);
        }

        // Resolve space ID
        Long spaceId = null;
        if (StringUtils.isNotBlank(request.getSpace())) {
            SpaceEntity space = spaceRepository.findBySlug(request.getSpace())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Space not found"));
            spaceId = space.getId();
        }

        // Generate slug
        String slug = generateSlug(request.getTitle() != null ? request.getTitle() : request.getMessage());

        // Render HTML from message
        String messageRendered = renderMessage(request.getMessage());

        // Build feed entity
        FeedPostEntity feed = FeedPostEntity.builder()
                .userId(userId)
                .title(request.getTitle())
                .slug(slug)
                .message(request.getMessage())
                .messageRendered(messageRendered)
                .type("text")
                .contentType(request.getContentType() != null ? request.getContentType() : "text")
                .spaceId(spaceId)
                .privacy(request.getPrivacy() != null ? request.getPrivacy() : "public")
                .status("published")
                .commentsCount(0)
                .reactionsCount(0)
                .isSticky(0)
                .priority(0)
                .build();

        // Check if this is user's first post (before saving)
        long existingPostCount = feedPostRepository.countByUserIdAndStatusPublished(userId);
        boolean isFirstPost = existingPostCount == 0;

        feed = feedPostRepository.save(feed);

        // Award points for publishing feed
        try{
            User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
            userPointsService.addPoints(user.getEmail(), 20, "POINT_FEED_PUBLISHED", feed.getId(), null);
        }catch(AppException e){
            log.error("Add Point Failed | Code: {} | Message: {} | User: {} | Feed: {}",
                    e.getErrorCode(), e.getMessage(), userId, feed.getId());
        }

        // Award points to affiliate if this is user's first post
        if (isFirstPost) {
            awardAffiliatePointsForFirstPost(userId, feed.getId());
        }

        // Handle media items
        if (request.getMediaItems() != null && !request.getMediaItems().isEmpty()) {
            handleMediaItems(feed.getId(), request.getMediaItems(), userId);
        }

        FeedResponse feedResponse = convertToFeedResponse(feed, userId);

        return FeedCreateResponse.builder()
                .feed(feedResponse)
                .message("Your post has been published")
                .lastFetchedTimestamp(System.currentTimeMillis() / 1000)
                .build();
    }

    // ============= UPDATE FEED =============

    @Transactional
    public FeedCreateResponse updateFeed(Long feedId, FeedUpdateRequest request, Long userId) {
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Feed not found"));

        // Check ownership
        if (!feed.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You don't have permission to update this feed");
        }

        // Update fields
        if (request.getMessage() != null) {
            feed.setMessage(request.getMessage());
            feed.setMessageRendered(renderMessage(request.getMessage()));
        }
        if (request.getTitle() != null) {
            feed.setTitle(request.getTitle());
        }
        if (request.getContentType() != null) {
            feed.setContentType(request.getContentType());
        }
        if (request.getPrivacy() != null) {
            feed.setPrivacy(request.getPrivacy());
        }

        // Update space
        if (StringUtils.isNotBlank(request.getSpace())) {
            SpaceEntity space = spaceRepository.findBySlug(request.getSpace())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Space not found"));
            feed.setSpaceId(space.getId());
        }

        feed = feedPostRepository.save(feed);

        // Handle media items
        if (request.getMediaItems() != null) {
            // Delete old media associations
            List<MediaArchiveEntity> oldMedia = mediaArchiveRepository.findByFeedId(feedId);
            oldMedia.forEach(media -> {
                media.setFeedId(null);
                mediaArchiveRepository.save(media);
            });

            // Add new media
            if (!request.getMediaItems().isEmpty()) {
                handleMediaItems(feed.getId(), request.getMediaItems(), userId);
            }
        }

        FeedResponse feedResponse = convertToFeedResponse(feed, userId);

        return FeedCreateResponse.builder()
                .feed(feedResponse)
                .message("Your post has been updated")
                .build();
    }

    // ============= PATCH FEED =============

    @Transactional
    public FeedCreateResponse patchFeed(Long feedId, FeedPatchRequest request, Long userId) {
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Feed not found"));

        // Check ownership or admin
        if (!feed.getUserId().equals(userId)) {
            // Check if user is admin of the space
            if (feed.getSpaceId() != null) {
                SpaceUserEntity spaceUser = spaceUserRepository.findBySpaceIdAndUserId(feed.getSpaceId(), userId)
                        .orElse(null);
                if (spaceUser == null || !"admin".equals(spaceUser.getRole())) {
                    throw new AppException(ErrorCode.UNAUTHORIZED, "You don't have permission to update this feed");
                }
            } else {
                throw new AppException(ErrorCode.UNAUTHORIZED, "You don't have permission to update this feed");
            }
        }

        // Update fields
        if (request.getIsSticky() != null) {
            feed.setIsSticky(request.getIsSticky());
        }

        feed = feedPostRepository.save(feed);

        FeedResponse feedResponse = convertToFeedResponse(feed, userId);

        return FeedCreateResponse.builder()
                .feed(feedResponse)
                .message("Feed updated")
                .build();
    }

    // ============= SOFT DELETE FEED =============

    @Transactional
    public FeedCreateResponse softDeleteFeed(Long feedId, Long userId) {
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Feed not found"));

        // Check ownership
        if (!feed.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You don't have permission to delete this feed");
        }

        // Check if already deleted
        if ("deleted".equals(feed.getStatus()) || "trashed".equals(feed.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY, "Feed already deleted");
        }

        // Soft delete - change status to "deleted"
        feed.setStatus("deleted");
        feed = feedPostRepository.save(feed);

        FeedResponse feedResponse = convertToFeedResponse(feed, userId);

        return FeedCreateResponse.builder()
                .feed(feedResponse)
                .message("Feed has been deleted successfully")
                .build();
    }

    // ============= COMMENTS =============

    @Transactional(readOnly = true)
    public CommentListResponse getFeedComments(Long feedId, Integer page, Integer perPage, String orderBy, Long commentId, Long currentUserId) {
        // Check if feed exists and is published
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feed not found"));
        
        // Check if feed is published (not deleted)
        if (!"published".equals(feed.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Feed not found");
        }

        // If commentId is provided, validate it exists and belongs to the feed
        Long parentId = null;
        if (commentId != null) {
            PostCommentEntity parentComment = postCommentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Comment not found"));
            
            // Check if parent comment belongs to the same feed
            if (!parentComment.getPostId().equals(feedId)) {
                throw new AppException(ErrorCode.NOT_FOUND, "Comment not found");
            }
            
            // Check if parent comment is published
            if (!"published".equals(parentComment.getStatus())) {
                throw new AppException(ErrorCode.NOT_FOUND, "Comment not found");
            }
            
            parentId = commentId;
        }

        // Pagination - same as feed (page starts from 1, convert to 0-based)
        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int perPageNum = (perPage != null && perPage > 0) ? perPage : 20;

        // Sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt"); // Default: created_at_asc
        if ("created_at_desc".equals(orderBy)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(pageNum, perPageNum, sort);

        // Get comments - if parentId is provided, get child comments; otherwise get root comments
        Page<PostCommentEntity> commentPage;
        if (parentId != null) {
            // Get child comments of the parent comment
            if ("created_at_desc".equals(orderBy)) {
                commentPage = postCommentRepository.findByPostIdAndParentIdAndStatusOrderByCreatedAtDesc(feedId, parentId, "published", pageable);
            } else {
                commentPage = postCommentRepository.findByPostIdAndParentIdAndStatusOrderByCreatedAtAsc(feedId, parentId, "published", pageable);
            }
        } else {
            // Get root comments (parent_id is null)
            if ("created_at_desc".equals(orderBy)) {
                commentPage = postCommentRepository.findByPostIdAndParentIdAndStatusOrderByCreatedAtDesc(feedId, null, "published", pageable);
            } else {
                commentPage = postCommentRepository.findByPostIdAndParentIdAndStatusOrderByCreatedAtAsc(feedId, null, "published", pageable);
            }
        }

        // Convert to response
        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(comment -> convertToCommentResponse(comment, currentUserId))
                .collect(Collectors.toList());

        // Build PageResponse using the standard format (same as feed)
        PageResponse<CommentResponse> commentsPageResponse = PageResponse.of(
                commentResponses, 
                pageable, 
                commentPage.getTotalElements()
        );

        return CommentListResponse.builder()
                .comments(commentsPageResponse)
                .build();
    }

    @Transactional
    public CommentDetailResponse createComment(Long feedId, CommentCreateRequest request, Long userId) {
        // Check daily comment limit
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCommentCount = postCommentRepository.countByUserIdAndCreatedAtAfter(userId, startOfDay);
        if (todayCommentCount >= DAILY_COMMENT_LIMIT) {
            throw new AppException(ErrorCode.DAILY_COMMENT_LIMIT_EXCEEDED);
        }

        // Check if feed exists and is published
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feed not found"));
        
        // Check if feed is published (not deleted)
        if (!"published".equals(feed.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Feed not found");
        }

        // If parent_id is provided, validate it
        if (request.getParentId() != null) {
            PostCommentEntity parentComment = postCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Invalid parent comment"));
            
            // Check if parent comment belongs to the same feed
            if (!parentComment.getPostId().equals(feedId)) {
                throw new AppException(ErrorCode.INVALID_KEY, "Invalid parent comment");
            }
        }

        // Render HTML from message
        String messageRendered = renderMessage(request.getMessage());

        // Build comment entity
        PostCommentEntity comment = PostCommentEntity.builder()
                .userId(userId)
                .postId(feedId)
                .parentId(request.getParentId())
                .message(request.getMessage())
                .messageRendered(messageRendered)
                .reactionsCount(0)
                .type("comment")
                .contentType("text")
                .status("published")
                .build();

        comment = postCommentRepository.save(comment);

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
            userPointsService.addPoints(user.getEmail(), 10, "POINT_COMMENT_ADDED", feed.getId(), comment.getId());
        }catch(AppException e){
            log.error("Add Point Failed (Comment) | Code: {} | User: {} | Comment: {}", e.getErrorCode(), userId, comment.getId());
        }

        // Handle media items for comment
        if (request.getMediaImages() != null && !request.getMediaImages().isEmpty()) {
            handleCommentMediaItems(comment.getId(), request.getMediaImages(), userId);
        }

        // Update comments count in feed
        long commentsCount = postCommentRepository.countByPostId(feedId);
        feed.setCommentsCount((int) commentsCount);
        feedPostRepository.save(feed);

        // Send notifications
        try {
            User commenterUser = userRepository.findById(userId)
                    .orElse(null);
            String commenterName = commenterUser != null && StringUtils.isNotBlank(commenterUser.getDisplayName())
                    ? commenterUser.getDisplayName() : "Ai đó";

            // Build route matching API endpoint: /api/feeds/{id}/by-id
            String baseRoute = "/api/feeds/" + feed.getId() + "/by-id";

            // If it's a reply to a comment, notify the comment owner
            if (request.getParentId() != null) {
                PostCommentEntity parentComment = postCommentRepository.findById(request.getParentId())
                        .orElse(null);
                if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                    String title = String.format("%s đã trả lời comment của bạn", commenterName);
                    String message ="";
                    
                    // Add comment anchor to scroll to the reply
                    String route = baseRoute + "#comment-" + comment.getId();

                    notificationService.createNotification(
                            parentComment.getUserId(),   // userId - comment owner
                            title,
                            message,
                            NotificationType.COMMENT_REPLY,
                            route,
                            comment.getId(),             // objectId - new comment ID
                            userId,                      // srcUserId - person who replied
                            SourceObjectType.COMMENT     // srcObjectType
                    );
                }
            }

            // Notify post owner if they're not the one who commented
            if (!feed.getUserId().equals(userId)) {
                String title = String.format("%s đã bình luận bài viết của bạn", commenterName);
                String message = "";
                
                // Add comment anchor to scroll to the new comment
                String route = baseRoute + "#comment-" + comment.getId();

                notificationService.createNotification(
                        feed.getUserId(),                // userId - post owner
                        title,
                        message,
                        NotificationType.POST_NEW_COMMENT,
                        route,
                        comment.getId(),                 // objectId - comment ID
                        userId,                         // srcUserId - person who commented
                        SourceObjectType.COMMENT        // srcObjectType
                );
            }
        } catch (Exception e) {
            log.error("Failed to send notification for comment: {}", e.getMessage(), e);
        }

        CommentResponse commentResponse = convertToCommentResponse(comment, userId);

        return CommentDetailResponse.builder()
                .comment(commentResponse)
                .message("Your comment has been published")
                .build();
    }

    @Transactional
    public CommentDetailResponse updateComment(Long feedId, Long commentId, CommentUpdateRequest request, Long userId) {
        // Check if feed exists
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feed not found"));

        // Check if comment exists and belongs to feed
        PostCommentEntity comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Comment not found"));

        if (!comment.getPostId().equals(feedId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Comment not found");
        }

        // Check if comment is published (not deleted)
        if (!"published".equals(comment.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Comment not found");
        }

        // Check ownership
        if (!comment.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You don't have permission to update this comment");
        }

        // Update comment message
        String messageRendered = renderMessage(request.getMessage());
        comment.setMessage(request.getMessage());
        comment.setMessageRendered(messageRendered);
        comment = postCommentRepository.save(comment);

        // Handle media items - similar to feed update
        if (request.getMediaImages() != null) {
            // Delete old media associations
            List<MediaArchiveEntity> oldMedia = mediaArchiveRepository.findBySubObjectId(commentId);
            oldMedia.forEach(media -> {
                // Only remove media that belongs to this comment
                if ("comment".equals(media.getObjectSource())) {
                    media.setSubObjectId(null);
                    media.setObjectSource("comment");
                    mediaArchiveRepository.save(media);
                }
            });

            // Add new media
            if (!request.getMediaImages().isEmpty()) {
                handleCommentMediaItems(comment.getId(), request.getMediaImages(), userId);
            }
        }

        CommentResponse commentResponse = convertToCommentResponse(comment, userId);

        return CommentDetailResponse.builder()
                .comment(commentResponse)
                .message("Your comment has been updated")
                .build();
    }

    @Transactional
    public CommentDetailResponse softDeleteComment(Long feedId, Long commentId, Long userId) {
        // Check if feed exists
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Feed not found"));

        // Check if comment exists and belongs to feed
        PostCommentEntity comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Comment not found"));

        if (!comment.getPostId().equals(feedId)) {
            throw new AppException(ErrorCode.INVALID_KEY, "Comment not found");
        }

        // Check ownership
        if (!comment.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You don't have permission to delete this comment");
        }

        // Check if already deleted
        if ("deleted".equals(comment.getStatus()) || "trashed".equals(comment.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY, "Comment already deleted");
        }

        // Soft delete - change status to "deleted"
        comment.setStatus("deleted");
        comment = postCommentRepository.save(comment);

        // Soft delete all child comments recursively
        List<PostCommentEntity> allDescendants = collectAllDescendants(commentId);
        for (PostCommentEntity child : allDescendants) {
            if (!"deleted".equals(child.getStatus()) && !"trashed".equals(child.getStatus())) {
                child.setStatus("deleted");
                postCommentRepository.save(child);
            }
        }

        // Update comments count in feed (only count published comments)
        long commentsCount = postCommentRepository.countByPostId(feedId);
        feed.setCommentsCount((int) commentsCount);
        feedPostRepository.save(feed);

        CommentResponse commentResponse = convertToCommentResponse(comment, userId);

        return CommentDetailResponse.builder()
                .comment(commentResponse)
                .message("Comment has been deleted successfully")
                .build();
    }

    @Transactional
    public CommentReactionResponse toggleCommentReaction(Long commentId, Long userId) {
        // Check if comment exists and is published
        PostCommentEntity comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Comment not found"));
        
        // Check if comment is published (not deleted)
        if (!"published".equals(comment.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Comment not found");
        }

        // Check if reaction already exists
        Optional<PostReactionEntity> existingReaction = postReactionRepository
                .findByUserIdAndObjectIdAndObjectType(userId, commentId, "comment");

        boolean liked;
        if (existingReaction.isPresent()) {
            // Unlike - delete reaction
            postReactionRepository.delete(existingReaction.get());
            liked = false;
        } else {
            // Like - create reaction
            PostReactionEntity reaction = PostReactionEntity.builder()
                    .userId(userId)
                    .objectId(commentId)
                    .objectType("comment")
                    .type("like")
                    .build();
            postReactionRepository.save(reaction);
            liked = true;
        }

        // Update reactions count
        long reactionsCount = postReactionRepository.countByObjectIdAndObjectType(commentId, "comment");
        comment.setReactionsCount((int) reactionsCount);
        postCommentRepository.save(comment);

        // Send notification to comment owner if they're not the one who liked
        if (liked && !comment.getUserId().equals(userId)) {
            try {
                User likerUser = userRepository.findById(userId)
                        .orElse(null);
                String likerName = likerUser != null && StringUtils.isNotBlank(likerUser.getDisplayName())
                        ? likerUser.getDisplayName() : "Ai đó";
                
                // Get feed to build route matching API endpoint
                FeedPostEntity feed = feedPostRepository.findById(comment.getPostId())
                        .orElse(null);

                // Build route: /api/feeds/{id}/by-id#comment-{id} or fallback to /api/feeds
                String route = feed != null
                    ? "/api/feeds/" + feed.getId() + "/by-id#comment-" + commentId
                    : "/api/feeds";

                String title =  String.format("%s đã thích comment của bạn", likerName);
                String message = "";
                
                notificationService.createNotification(
                        comment.getUserId(),             // userId - comment owner
                        title,
                        message,
                        NotificationType.COMMENT_NEW_REACTION,
                        route,
                        commentId,                      // objectId - comment ID
                        userId,                         // srcUserId - person who liked
                        SourceObjectType.COMMENT        // srcObjectType
                );
            } catch (Exception e) {
                log.error("Failed to send notification for comment reaction: {}", e.getMessage(), e);
            }
        }

        return CommentReactionResponse.builder()
                .commentId(commentId)
                .liked(liked)
                .reactionsCount(reactionsCount)
                .build();
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId, Long currentUserId) {
        PostCommentEntity comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Comment not found"));

        // Check if comment is published (not deleted)
        if (!"published".equals(comment.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Comment not found");
        }

        CommentResponse commentResponse = convertToCommentResponse(comment, currentUserId);

        // Get all child comments (replies) - only direct children, ordered by created_at ASC
        List<PostCommentEntity> childComments = postCommentRepository
                .findByPostIdAndParentIdAndStatus(comment.getPostId(), commentId, "published");
        
        // Sort by created_at ASC
        childComments.sort(Comparator.comparing(PostCommentEntity::getCreatedAt));

        // Convert child comments to response
        List<CommentResponse> replies = childComments.stream()
                .map(childComment -> convertToCommentResponse(childComment, currentUserId))
                .collect(Collectors.toList());

        commentResponse.setReplies(replies);

        return commentResponse;
    }

    // ============= REACTION (LIKE/UNLIKE) =============

    @Transactional
    public FeedBookmarkResponse toggleBookmark(Long feedId, Long userId) {
        // Check if feed exists and is published
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feed not found"));

        if (!"published".equals(feed.getStatus())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Feed not found");
        }

        // Check if bookmark already exists (using type = "bookmark" and objectType = "feed")
        Optional<PostReactionEntity> existingBookmark = postReactionRepository
                .findByUserIdAndObjectIdAndObjectTypeAndType(userId, feedId, "feed", "bookmark");

        boolean bookmarked;
        if (existingBookmark.isPresent()) {
            // Unbookmark - delete bookmark
            postReactionRepository.delete(existingBookmark.get());
            bookmarked = false;
        } else {
            // Bookmark - create bookmark entry
            PostReactionEntity bookmark = PostReactionEntity.builder()
                    .userId(userId)
                    .objectId(feedId)
                    .objectType("feed")
                    .type("bookmark")
                    .build();
            postReactionRepository.save(bookmark);
            bookmarked = true;
        }

        return FeedBookmarkResponse.builder()
                .feedId(feedId)
                .bookmarked(bookmarked)
                .message(bookmarked ? "Feed bookmarked successfully" : "Feed unbookmarked successfully")
                .build();
    }

    @Transactional(noRollbackFor = AppException.class)
    public FeedReactionResponse toggleReaction(Long feedId, Long userId) {
        // Check if feed exists
        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Feed not found"));

        // Check if reaction already exists
        Optional<PostReactionEntity> existingReaction = postReactionRepository
                .findByUserIdAndObjectIdAndObjectType(userId, feedId, "feed");

        boolean liked;
        if (existingReaction.isPresent()) {
            // Unlike - delete reaction
            postReactionRepository.delete(existingReaction.get());
            liked = false;

            // Remove points for unliking
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    userPointsService.removePoints(user.getEmail(), 1, "POINT_REACTION_LIKE", feedId, null);
                }
            } catch (Exception e) {
                log.error("Remove Point Failed (Unlike) | User: {} | Feed: {} | Message: {}",
                    userId, feedId, e.getMessage());
            }
        } else {
            // Check daily reaction limit (only when creating new reaction)
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            long todayReactionCount = postReactionRepository.countByUserIdAndCreatedAtAfter(userId, startOfDay);
            if (todayReactionCount >= DAILY_REACTION_LIMIT) {
                throw new AppException(ErrorCode.DAILY_REACTION_LIMIT_EXCEEDED);
            }

            // Like - create reaction
            PostReactionEntity reaction = PostReactionEntity.builder()
                    .userId(userId)
                    .objectId(feedId)
                    .objectType("feed")
                    .type("like")
                    .build();
            postReactionRepository.save(reaction);
            liked = true;

            // Award points for liking
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    userPointsService.addPoints(user.getEmail(), 1, "POINT_REACTION_LIKE", feedId, null);
                }
            } catch (Exception e) {
                log.error("Add Point Failed (Like) | Code: {} | User: {} | Feed: {} | Message: {}",
                    e instanceof AppException ? ((AppException) e).getErrorCode() : "UNKNOWN",
                    userId, feedId, e.getMessage());
            }

            // Send notification to post owner if they're not the one who liked
            if (!feed.getUserId().equals(userId)) {
                try {
                    User likerUser = userRepository.findById(userId)
                            .orElse(null);
                    String likerName = likerUser != null && StringUtils.isNotBlank(likerUser.getDisplayName())
                            ? likerUser.getDisplayName() : "Ai đó";

                    String title = String.format("%s đã thích bài viết của bạn", likerName);
                    String message = "";

                    // Build route matching API endpoint: /api/feeds/{id}/by-id
                    String route = "/api/feeds/" + feed.getId() + "/by-id";

                    notificationService.createNotification(
                            feed.getUserId(),           // userId - post owner
                            title,
                            message,
                            NotificationType.POST_NEW_REACTION,
                            route,
                            feedId,                     // objectId - feed ID
                            userId,                     // srcUserId - person who liked
                            SourceObjectType.FEED       // srcObjectType
                    );
                } catch (Exception e) {
                    log.error("Failed to send notification for post reaction: {}", e.getMessage(), e);
                }
            }
        }

        // Update reactions count
        long reactionsCount = postReactionRepository.countByObjectIdAndObjectType(feedId, "feed");
        feed.setReactionsCount((int) reactionsCount);
        feedPostRepository.save(feed);

        return FeedReactionResponse.builder()
                .feedId(feedId)
                .liked(liked)
                .reactionsCount(reactionsCount)
                .build();
    }

    public FeedReactionResponse checkReactionStatus(Long feedId, Long userId) {

        FeedPostEntity feed = feedPostRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Feed not found"));


        boolean liked = postReactionRepository
                .findByUserIdAndObjectIdAndObjectType(userId, feedId, "feed")
                .isPresent();


        long reactionsCount = postReactionRepository.countByObjectIdAndObjectType(feedId, "feed");

        return FeedReactionResponse.builder()
                .feedId(feedId)
                .liked(liked)
                .reactionsCount(reactionsCount)
                .build();
    }


    // ============= PRIVATE HELPER METHODS =============

    /**
     * Cộng 60 điểm cho affiliate khi người được giới thiệu đăng bài đầu tiên
     * @param userId ID của user vừa đăng bài
     * @param feedId ID của feed vừa được tạo
     */
    private void awardAffiliatePointsForFirstPost(Long userId, Long feedId) {
        try {
            // Lấy affiliate_id từ UserMeta
            UserMetaEntity affiliateMeta = userMetaRepository.findByUserIdAndMetaKey(userId, "affiliate_id")
                    .orElse(null);
            
            if (affiliateMeta == null || StringUtils.isBlank(affiliateMeta.getMetaValue())) {
                log.debug("User {} has no affiliate_id. Skipping affiliate points for first post.", userId);
                return;
            }

            Long affiliateId;
            try {
                affiliateId = Long.parseLong(affiliateMeta.getMetaValue().trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid affiliate_id format for user {}: {}", userId, affiliateMeta.getMetaValue());
                return;
            }

            // Tìm affiliate
            Affiliate affiliate = affiliateRepository.findById(affiliateId)
                    .orElse(null);
            
            if (affiliate == null) {
                log.warn("Affiliate with id {} not found. Skipping points award for first post.", affiliateId);
                return;
            }

            // Lấy user của affiliate để cộng điểm
            User affiliateUser = userRepository.findById(affiliate.getUserId())
                    .orElse(null);
            
            if (affiliateUser == null) {
                log.warn("User with id {} (affiliate user) not found. Skipping points award for first post.", affiliate.getUserId());
                return;
            }

            // Cộng 60 điểm cho affiliate
            // Sử dụng relatedId là feedId để tránh cộng điểm trùng lặp
            try {
                userPointsService.addPoints(
                    affiliateUser.getEmail(), 
                    60, 
                    "POINT_AFFILIATE_FIRST_POST", 
                    feedId, 
                    userId
                );
                log.info("Awarded 60 points to affiliate {} (user: {}) for first post by referred user (userId: {}, feedId: {})", 
                    affiliateId, affiliateUser.getEmail(), userId, feedId);
            } catch (AppException e) {
                log.info("Affiliate points for first post already awarded for affiliate {} and feed {}. Skipping.", 
                    affiliateId, feedId);
            }
        } catch (Exception e) {
            log.error("Error awarding affiliate points for first post (userId: {}, feedId: {}): {}", 
                userId, feedId, e.getMessage(), e);
        }
    }

    private FeedResponse convertToFeedResponse(FeedPostEntity feed, Long currentUserId) {
        // Get xprofile
        XProfileResponse xprofile = getXProfile(feed.getUserId());

        // Get space
        SpaceInfoResponse space = null;
        if (feed.getSpaceId() != null) {
            space = spaceRepository.findById(feed.getSpaceId())
                    .map(s -> SpaceInfoResponse.builder()
                            .id(s.getId())
                            .title(s.getTitle())
                            .slug(s.getSlug())
                            .type(s.getType())
                            .build())
                    .orElse(null);
        }

        // Get media
        List<MediaResponse> media = mediaArchiveRepository.findByFeedId(feed.getId())
                .stream()
                .map(this::convertToMediaResponse)
                .collect(Collectors.toList());

        // Get interactions
        FeedInteractionsResponse interactions = FeedInteractionsResponse.builder()
                .like(currentUserId != null && postReactionRepository
                        .findByUserIdAndObjectIdAndObjectType(currentUserId, feed.getId(), "feed")
                        .isPresent())
                .bookmark(currentUserId != null && postReactionRepository
                        .findByUserIdAndObjectIdAndObjectTypeAndType(currentUserId, feed.getId(), "feed", "bookmark")
                        .isPresent())
                .build();

        return FeedResponse.builder()
                .id(feed.getId())
                .userId(feed.getUserId())
                .title(feed.getTitle())
                .slug(feed.getSlug())
                .message(feed.getMessage())
                .messageRendered(feed.getMessageRendered())
                .type(feed.getType())
                .contentType(feed.getContentType())
                .spaceId(feed.getSpaceId())
                .privacy(feed.getPrivacy())
                .status(feed.getStatus())
                .featuredImage(feed.getFeaturedImage())
                .commentsCount(feed.getCommentsCount())
                .reactionsCount(feed.getReactionsCount())
                .isSticky(feed.getIsSticky())
                .priority(feed.getPriority())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .xprofile(xprofile)
                .space(space)
                .media(media)
                .interactions(interactions)
                .build();
    }

    private CommentResponse convertToCommentResponse(PostCommentEntity comment, Long currentUserId) {
        XProfileResponse xprofile = getXProfile(comment.getUserId());

        // Check if current user liked this comment
        Integer liked = 0;
        if (currentUserId != null) {
            liked = postReactionRepository.findByUserIdAndObjectIdAndObjectType(currentUserId, comment.getId(), "comment")
                    .isPresent() ? 1 : 0;
        }

        // Get media for comment
        List<MediaResponse> media = mediaArchiveRepository.findBySubObjectId(comment.getId())
                .stream()
                .map(this::convertToMediaResponse)
                .collect(Collectors.toList());

        // Count number of child comments (replies)
        long repliesCount = postCommentRepository.countByParentId(comment.getId());

        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .message(comment.getMessage())
                .messageRendered(comment.getMessageRendered())
                .reactionsCount(comment.getReactionsCount())
                .repliesCount(repliesCount)
                .type(comment.getType() != null ? comment.getType() : "comment")
                .contentType(comment.getContentType() != null ? comment.getContentType() : "text")
                .status(comment.getStatus() != null ? comment.getStatus() : "published")
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .xprofile(xprofile)
                .liked(liked)
                .media(media)
                .build();
    }

    private ReactionResponse convertToReactionResponse(PostReactionEntity reaction) {
        XProfileResponse xprofile = getXProfile(reaction.getUserId());

        return ReactionResponse.builder()
                .id(reaction.getId())
                .userId(reaction.getUserId())
                .type(reaction.getType())
                .xprofile(xprofile)
                .build();
    }

    private MediaResponse convertToMediaResponse(MediaArchiveEntity media) {
        // Parse settings JSON to get width and height
        Integer width = null;
        Integer height = null;
        if (StringUtils.isNotBlank(media.getSettings())) {
            try {
                Map<String, Object> settings = objectMapper.readValue(media.getSettings(), Map.class);
                if (settings.containsKey("width")) {
                    width = settings.get("width") instanceof Integer ? (Integer) settings.get("width") : 
                            Integer.parseInt(settings.get("width").toString());
                }
                if (settings.containsKey("height")) {
                    height = settings.get("height") instanceof Integer ? (Integer) settings.get("height") : 
                            Integer.parseInt(settings.get("height").toString());
                }
            } catch (Exception e) {
                log.warn("Failed to parse media settings: {}", e.getMessage());
            }
        }

        // Build full URL with appDomain if mediaUrl is relative path
        String mediaUrl = media.getMediaUrl();
        if (mediaUrl != null && mediaUrl.startsWith("/") && !mediaUrl.startsWith("http://") && !mediaUrl.startsWith("https://")) {
            mediaUrl = appDomain + mediaUrl;
        }

        return MediaResponse.builder()
                .id(media.getId())
                .mediaKey(media.getMediaKey())
                .url(mediaUrl)
                .type(media.getMediaType())
                .width(width)
                .height(height)
                .build();
    }

    private XProfileResponse getXProfile(Long userId) {
        // Try to get from XProfile first
        Optional<XProfileEntity> xProfileOpt = xProfileRepository.findByUserId(userId);
        
        // Get User entity for fallback data
        Optional<User> userOpt = userRepository.findById(userId);
        
        // Get UserMeta for avatar (url_image)
        String avatarFromMeta = null;
        List<UserMetaEntity> userMetas = userMetaRepository.findByUserId(userId);
        for (UserMetaEntity meta : userMetas) {
            if ("url_image".equals(meta.getMetaKey())) {
                avatarFromMeta = meta.getMetaValue();
                break;
            }
        }
        
        // Make it final for lambda usage
        final String finalAvatarFromMeta = avatarFromMeta;
        
        if (xProfileOpt.isPresent()) {
            XProfileEntity xp = xProfileOpt.get();
            // Use avatar from UserMeta if XProfile avatar is null, otherwise use XProfile avatar
            String finalAvatar = StringUtils.isNotBlank(xp.getAvatar()) ? xp.getAvatar() : finalAvatarFromMeta;
            
            // Get fullName from User.displayName
            String fullName = userOpt.map(User::getDisplayName).orElse(null);
            
            return XProfileResponse.builder()
                    .userId(xp.getUserId())
                    .username(xp.getUsername())
                    .displayName(xp.getDisplayName())
                    .fullName(fullName)
                    .avatar(finalAvatar)
                    .totalPoints(xp.getTotalPoints())
                    .isVerified(xp.getIsVerified() != null && xp.getIsVerified() == 1)
                    .build();
        } else {
            // Fallback to User entity if xprofile doesn't exist
            return userOpt.map(user -> XProfileResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .fullName(user.getDisplayName()) // Use displayName as fullName
                    .avatar(finalAvatarFromMeta) // Get from UserMeta
                    .totalPoints(0)
                    .isVerified(false)
                    .build())
                    .orElse(XProfileResponse.builder()
                            .userId(userId)
                            .username("")
                            .displayName("")
                            .fullName("")
                            .avatar(null)
                            .totalPoints(0)
                            .isVerified(false)
                            .build());
        }
    }

    private String generateSlug(String text) {
        if (StringUtils.isBlank(text)) {
            return UUID.randomUUID().toString();
        }
        String slug = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return slug + "-" + timestamp;
    }

    private String renderMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return "";
        }
        // Simple HTML rendering - wrap in <p> tags and convert newlines to <br>
        String rendered = message.replace("\n", "<br>");
        if (!rendered.startsWith("<")) {
            rendered = "<p>" + rendered + "</p>";
        }
        return HtmlUtil.processHtml(rendered);
    }

    private void handleMediaItems(Long feedId, List<FeedCreateRequest.MediaItemRequest> mediaItems, Long userId) {
        for (FeedCreateRequest.MediaItemRequest item : mediaItems) {
            if (StringUtils.isNotBlank(item.getMediaKey())) {
                mediaArchiveRepository.findByMediaKey(item.getMediaKey())
                        .ifPresent(media -> {
                            media.setFeedId(feedId);
                            media.setObjectSource("feed");
                            mediaArchiveRepository.save(media);
                        });
            }
        }
    }

    private void handleCommentMediaItems(Long commentId, List<CommentCreateRequest.MediaItemRequest> mediaItems, Long userId) {
        for (CommentCreateRequest.MediaItemRequest item : mediaItems) {
            if (item != null && StringUtils.isNotBlank(item.getMediaKey())) {
                mediaArchiveRepository.findByMediaKey(item.getMediaKey())
                        .ifPresent(media -> {
                            media.setSubObjectId(commentId);
                            media.setObjectSource("comment");
                            mediaArchiveRepository.save(media);
                        });
            }
        }
    }

    /**
     * Collect all descendant comments recursively (all children, grandchildren, etc.)
     */
    private List<PostCommentEntity> collectAllDescendants(Long parentId) {
        List<PostCommentEntity> allDescendants = new ArrayList<>();
        collectDescendantsRecursive(parentId, allDescendants);
        return allDescendants;
    }

    /**
     * Recursively collect all child comments
     */
    private void collectDescendantsRecursive(Long parentId, List<PostCommentEntity> result) {
        List<PostCommentEntity> children = postCommentRepository.findByParentId(parentId);
        for (PostCommentEntity child : children) {
            result.add(child);
            // Recursively collect children of this child
            collectDescendantsRecursive(child.getId(), result);
        }
    }

}

