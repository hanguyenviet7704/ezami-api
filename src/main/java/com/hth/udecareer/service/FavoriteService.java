package com.hth.udecareer.service;

import com.hth.udecareer.entities.FavoriteEntity;
import com.hth.udecareer.entities.Post;
import com.hth.udecareer.entities.PostMeta;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.FavoritableType;
import com.hth.udecareer.enums.FavoriteStatus;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.projection.PostDataProjection;
import com.hth.udecareer.model.request.AddFavoriteRequest;
import com.hth.udecareer.model.request.FavoriteFilterRequest;
import com.hth.udecareer.model.response.FavoriteCheckResponse;
import com.hth.udecareer.model.response.FavoriteResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.FavoriteRepository;
import com.hth.udecareer.repository.PostMetaRepository;
import com.hth.udecareer.repository.PostRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Service quản lý favorites của user
 * - Hỗ trợ CRUD operations
 * - Auto-detect favoritable type từ wp_posts
 * - Enrich data với post info và featured images
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;

    private static final String THUMBNAIL_META_KEY = "_thumbnail_id";
    private static final String ATTACHMENT_TYPE = "attachment";
    private static final int DEFAULT_PAGE_SIZE = 20;

    // ==================== Public Methods ====================

    /**
     * Lấy danh sách favorites với filtering và pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getFavorites(String email, FavoriteFilterRequest filter) {
        // Validate input
        validateEmail(email);
        validateFavoriteFilter(filter);
        
        User user = getUserByEmail(email);
        Pageable pageable = createPageable(filter);
        
        // Query favorites from database
        QueryResult queryResult = queryFavorites(user.getId(), filter, pageable);
        
        // Enrich với post data và featured images
        List<FavoriteResponse> responses = enrichFavoriteEntities(queryResult.favorites);
        
        if (responses.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        
        // Apply keyword filter
        responses = applyKeywordFilter(responses, filter.getKeyword());
        
        if (responses.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        
        return buildPageResponse(responses, pageable, queryResult.total, filter);
    }

    /**
     * Thêm favorite mới (auto-detect type từ wp_posts)
     * Nếu là QUIZ: favoritableId = quiz_pro_id, lưu trực tiếp quiz_pro_id
     * Nếu là COURSE/LESSON/POST/TOPIC: favoritableId = post_id
     */
    @Transactional
    public FavoriteResponse addFavorite(String email, AddFavoriteRequest request) {
        // Validate input
        validateEmail(email);
        validateAddFavoriteRequest(request);
        
        User user = getUserByEmail(email);
        Long inputId = request.getFavoritableId(); // Có thể là quiz_pro_id hoặc post_id
        
        // Bước 1: Detect type từ ID này (thử cả quiz_pro_id và post_id)
        FavoritableType type = detectFavoritableTypeFromInput(inputId);
        
        // Bước 2: Xác định ID cần lưu vào database
        // - QUIZ: Lưu quiz_pro_id (inputId)
        // - Others: Lưu post_id (inputId)
        Long favoritableId = inputId; // Giữ nguyên input
        
        // Bước 3: Check existing favorite
        Optional<FavoriteEntity> existing = favoriteRepository
                .findByUserIdAndTypeAndFavoritableId(user.getId(), type.getValue(), favoritableId);
        
        FavoriteEntity favorite;
        if (existing.isPresent()) {
            favorite = existing.get();
            if (favorite.getStatus() == FavoriteStatus.DELETED) {
                favorite.restore();
                favorite = favoriteRepository.save(favorite);
            }
        } else {
            favorite = createNewFavorite(user.getId(), type, favoritableId);
        }
        
        return toResponse(favorite);
    }

    /**
     * Xóa favorite (soft delete)
     */
    @Transactional
    public void deleteFavorite(String email, Long Id) {
        // Validate input
        validateEmail(email);
        validateFavoriteId(Id);
        
        User user = getUserByEmail(email);
        FavoriteEntity favorite = favoriteRepository.findById(Id)
                .orElseThrow(() -> new AppException(ErrorCode.FAVORITE_NOT_FOUND));
        
        if (!favorite.getUserId().equals(user.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        
        favorite.softDelete();
        favoriteRepository.save(favorite);
    }



    /**
     * Xóa favorite theo favoritableId (soft delete)
     * Use case: Client chỉ biết ID của post/course/quiz, không biết ID của favorite record
     * Nếu là QUIZ: favoriteId = quiz_pro_id (đã lưu trong DB)
     * Nếu là COURSE/LESSON/POST/TOPIC: favoriteId = post_id
     */
    @Transactional
    public void deleteFavoriteByFavoritableId(String email, Long favoriteId) {
        // Validate input
        validateEmail(email);
        validateFavoritableId(favoriteId);

        User user = getUserByEmail(email);
        
        // Không cần detect hay convert, vì database đã lưu đúng ID mà client gửi
        // Tìm favorite trực tiếp bằng favoriteId
        FavoriteEntity favorite = favoriteRepository.findActiveFavoriteByUserIdAndFavoritableId(user.getId(), favoriteId)
                .orElseThrow(() -> new AppException(ErrorCode.FAVORITE_NOT_FOUND));

        if (!favorite.getUserId().equals(user.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        favorite.softDelete();
        favoriteRepository.save(favorite);
    }


    /**
     * Check favorite có tồn tại không
     */
    @Transactional(readOnly = true)
    public FavoriteCheckResponse checkFavoriteExists(String email, Long favoritableId) {
        // Validate input
        validateEmail(email);
        validateFavoritableId(favoritableId);
        
        User user = getUserByEmail(email);
        Optional<FavoriteEntity> favorite = favoriteRepository
                .findActiveFavoriteByUserIdAndFavoritableId(user.getId(), favoritableId);
        
        return FavoriteCheckResponse.builder()
                .isFavorited(favorite.isPresent())
                .favoriteId(favorite.map(FavoriteEntity::getId).orElse(null))
                .build();
    }

    // ==================== Private Helper Methods ====================
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
    }
    
    private Pageable createPageable(FavoriteFilterRequest filter) {
        boolean usePagination = filter.getPage() != null || filter.getSize() != null;
        
        if (usePagination) {
            return PageRequest.of(
                    filter.getPage() != null ? filter.getPage() : 0,
                    filter.getSize() != null ? filter.getSize() : DEFAULT_PAGE_SIZE
            );
        }
        
        return Pageable.unpaged();
    }
    
    private QueryResult queryFavorites(Long userId, FavoriteFilterRequest filter, Pageable pageable) {
        LocalDateTime fromDateTime = filter.getFromDate() != null 
                ? LocalDateTime.of(filter.getFromDate(), LocalTime.MIN) : null;
        LocalDateTime toDateTime = filter.getToDate() != null 
                ? LocalDateTime.of(filter.getToDate(), LocalTime.MAX) : null;
        
        boolean hasTypeFilter = filter.getType() != null;
        boolean hasDateFilter = fromDateTime != null || toDateTime != null;
        String statusValue = FavoriteStatus.ACTIVE.getValue();
        
        List<FavoriteEntity> favorites;
        long total;
        
        if (hasTypeFilter && hasDateFilter) {
            String typeValue = filter.getType().getValue();
            favorites = favoriteRepository.findByUserIdAndTypeAndStatusWithDateRange(
                    userId, typeValue, statusValue, fromDateTime, toDateTime, pageable);
            total = favoriteRepository.countByUserIdAndTypeAndStatusWithDateRange(
                    userId, typeValue, statusValue, fromDateTime, toDateTime);
                    
        } else if (hasTypeFilter) {
            String typeValue = filter.getType().getValue();
            favorites = favoriteRepository.findByUserIdAndTypeAndStatusOptimized(
                    userId, typeValue, statusValue, pageable);
            total = favoriteRepository.countByUserIdAndTypeAndStatus(
                    userId, typeValue, statusValue);
                    
        } else if (hasDateFilter) {
            favorites = favoriteRepository.findByUserIdAndStatusWithDateRange(
                    userId, statusValue, fromDateTime, toDateTime, pageable);
            total = favoriteRepository.countByUserIdAndStatusWithDateRange(
                    userId, statusValue, fromDateTime, toDateTime);
                    
        } else {
            favorites = favoriteRepository.findByUserIdAndStatusOptimized(
                    userId, statusValue, pageable);
            total = favoriteRepository.countByUserIdAndStatus(userId, statusValue);
        }
        
        return new QueryResult(favorites, total);
    }
    
    private PageResponse<FavoriteResponse> buildPageResponse(
            List<FavoriteResponse> responses, 
            Pageable pageable, 
            long total,
            FavoriteFilterRequest filter) {
        
        boolean usePagination = filter.getPage() != null || filter.getSize() != null;
        
        if (usePagination) {
            return PageResponse.of(responses, pageable, total);
        }
        
        return PageResponse.<FavoriteResponse>builder()
                .content(responses)
                .page(0)
                .size(responses.size())
                .totalElements((long) responses.size())
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .first(true)
                .last(true)
                .build();
    }
    
    private FavoritableType detectFavoritableType(Long favoritableId) {
        String postType = postRepository.findPostTypeById(
                favoritableId,
                List.of(PostStatus.PUBLISH, PostStatus.PRIVATE)
        ).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        
        return parsePostTypeToFavoritableType(postType);
    }
    
    private FavoriteEntity createNewFavorite(Long userId, FavoritableType type, Long favoritableId) {
        FavoriteEntity favorite = FavoriteEntity.builder()
                .userId(userId)
                .favoritableType(type)
                .favoritableId(favoritableId)
                .status(FavoriteStatus.ACTIVE)
                .build();
        return favoriteRepository.save(favorite);
    }
    
    private FavoriteResponse toResponse(FavoriteEntity favorite) {
        // Không cần convert vì database đã lưu đúng ID
        // QUIZ: favoritable_id = quiz_pro_id
        // Others: favoritable_id = post_id
        
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .favoriteType(favorite.getFavoritableType().getValue())
                .favoriteId(favorite.getFavoritableId()) // Trả về đúng ID đã lưu
                .status(favorite.getStatus().getValue())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    /**
     * Apply keyword filter in-memory
     */
    private List<FavoriteResponse> applyKeywordFilter(List<FavoriteResponse> responses, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return responses;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return responses.stream()
                .filter(r -> matchesKeyword(r, lowerKeyword))
                .toList();
    }
    
    private boolean matchesKeyword(FavoriteResponse response, String lowerKeyword) {
        return (response.getTitle() != null && response.getTitle().toLowerCase().contains(lowerKeyword)) ||
               (response.getCategoryTitle() != null && response.getCategoryTitle().toLowerCase().contains(lowerKeyword));
    }

    /**
     * Enrich favorite entities với data từ wp_posts và featured images
     */
    private List<FavoriteResponse> enrichFavoriteEntities(List<FavoriteEntity> favorites) {
        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Convert quiz_pro_id to post_id for QUIZ favorites
        List<Long> postIds = favorites.stream()
                .map(fav -> {
                    if (fav.getFavoritableType() == FavoritableType.QUIZ) {
                        // favoritable_id là quiz_pro_id, cần convert sang post_id
                        return findPostIdByQuizProId(fav.getFavoritableId())
                                .orElse(fav.getFavoritableId()); // Fallback to original ID if not found
                    } else {
                        // favoritable_id đã là post_id
                        return fav.getFavoritableId();
                    }
                })
                .distinct()
                .toList();
        
        // Fetch post data and featured images
        Map<Long, PostDataProjection> postDataMap = getPostDataMap(postIds);
        Map<Long, String> featuredImageMap = getFeaturedImagesForPosts(postIds);
        
        // Build responses
        return favorites.stream()
                .map(favorite -> buildFavoriteResponse(favorite, postDataMap, featuredImageMap))
                .filter(Objects::nonNull)
                .toList();
    }
    
    private Map<Long, PostDataProjection> getPostDataMap(List<Long> postIds) {
        List<PostDataProjection> postDataList = postRepository.findPostDataByIds(postIds);
        Map<Long, PostDataProjection> map = new HashMap<>();
        postDataList.forEach(data -> map.put(data.getPostId(), data));
        return map;
    }
    
    private FavoriteResponse buildFavoriteResponse(
            FavoriteEntity favorite,
            Map<Long, PostDataProjection> postDataMap,
            Map<Long, String> featuredImageMap) {
        
        Long favoritableId = favorite.getFavoritableId(); // quiz_pro_id cho QUIZ, post_id cho others
        
        // Lấy post_id để query post data
        Long postId = favoritableId;
        if (favorite.getFavoritableType() == FavoritableType.QUIZ) {
            // Convert quiz_pro_id to post_id
            postId = findPostIdByQuizProId(favoritableId).orElse(favoritableId);
        }
        
        PostDataProjection postData = postDataMap.get(postId);
        
        if (postData == null) {
            return null; // Skip if post not found
        }
        
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .favoriteType(favorite.getFavoritableType().getValue())
                .favoriteId(favoritableId) // Trả về quiz_pro_id cho QUIZ, post_id cho others
                .status(favorite.getStatus().getValue())
                .createdAt(favorite.getCreatedAt())
                .title(postData.getPostTitle())
                .postStatus(postData.getPostStatus())
                .categoryCode(postData.getCategoryCode())
                .categoryTitle(postData.getCategoryTitle())
                .categoryImageUri(postData.getCategoryImageUri())
                .featuredImage(featuredImageMap.get(postId))
                .build();
    }

    /**
     * Lấy featured images cho danh sách post IDs
     */
    private Map<Long, String> getFeaturedImagesForPosts(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // Get thumbnail meta
        List<PostMeta> thumbnailMetas = postMetaRepository.findAllByPostIdInAndMetaKey(
                postIds, THUMBNAIL_META_KEY);
        
        if (thumbnailMetas.isEmpty()) {
            return new HashMap<>();
        }
        
        // Extract attachment IDs
        List<Long> attachmentIds = thumbnailMetas.stream()
                .map(meta -> parseAttachmentId(meta.getMetaValue()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        
        if (attachmentIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // Get attachment URLs
        Map<Long, String> attachmentUrlMap = getAttachmentUrls(attachmentIds);
        
        // Map postId -> featuredImageUrl
        Map<Long, String> featuredImageMap = new HashMap<>();
        thumbnailMetas.forEach(meta -> {
            Long attachmentId = parseAttachmentId(meta.getMetaValue());
            if (attachmentId != null) {
                String imageUrl = attachmentUrlMap.get(attachmentId);
                if (imageUrl != null) {
                    featuredImageMap.put(meta.getPostId(), imageUrl);
                }
            }
        });
        
        return featuredImageMap;
    }
    
    private Long parseAttachmentId(String metaValue) {
        if (metaValue == null || metaValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(metaValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Map<Long, String> getAttachmentUrls(List<Long> attachmentIds) {
        List<Post> attachments = postRepository.findAllById(attachmentIds);
        Map<Long, String> urlMap = new HashMap<>();
        
        attachments.forEach(att -> {
            if (ATTACHMENT_TYPE.equals(att.getType())) {
                urlMap.put(att.getId(), att.getGuid());
            }
        });
        
        return urlMap;
    }

    /**
     * Parse post_type từ wp_posts thành FavoritableType
     * Examples: sfwd-courses -> COURSE, sfwd-lessons -> LESSON, post -> POST
     */
    private FavoritableType parsePostTypeToFavoritableType(String postType) {
        if (postType == null || postType.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_POST_TYPE);
        }

        // Extract type from post_type (e.g., sfwd-courses -> courses)
        String typeString = postType.contains("-") 
                ? postType.substring(postType.lastIndexOf("-") + 1)
                : postType;

        // Remove trailing 's' (courses -> course, lessons -> lesson)
        if (typeString.endsWith("s") && !typeString.equals("quiz")) {
            typeString = typeString.substring(0, typeString.length() - 1);
        }

        try {
            return FavoritableType.fromString(typeString);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_POST_TYPE);
        }
    }

    /**
     * Detect FavoritableType từ input ID (có thể là post_id hoặc quiz_pro_id)
     * Thử detect theo thứ tự: post_id trước, nếu không có thì thử quiz_pro_id
     */
    private FavoritableType detectFavoritableTypeFromInput(Long inputId) {
        // Bước 1: Thử detect từ wp_posts.id (post_id)
        Optional<String> postType = postRepository.findPostTypeById(
                inputId,
                List.of(PostStatus.PUBLISH, PostStatus.PRIVATE)
        );
        
        if (postType.isPresent()) {
            return parsePostTypeToFavoritableType(postType.get());
        }
        
        // Bước 2: Nếu không tìm thấy, thử tìm từ wp_postmeta.meta_value (quiz_pro_id)
        Optional<Long> postIdFromQuizProId = findPostIdByQuizProId(inputId);
        if (postIdFromQuizProId.isPresent()) {
            return FavoritableType.QUIZ;
        }
        
        // Nếu không tìm thấy cả 2, throw exception
        throw new AppException(ErrorCode.POST_NOT_FOUND);
    }

    /**
     * Convert input ID sang post_id
     * - Nếu type là QUIZ và input là quiz_pro_id: convert sang post_id
     * - Ngược lại: giữ nguyên (input đã là post_id)
     */
    private Long convertToPostId(Long inputId, FavoritableType type) {
        if (type != FavoritableType.QUIZ) {
            return inputId; // Không phải QUIZ, giữ nguyên
        }
        
        // Kiểm tra xem inputId có phải là post_id không
        Optional<String> postType = postRepository.findPostTypeById(
                inputId,
                List.of(PostStatus.PUBLISH, PostStatus.PRIVATE)
        );
        
        if (postType.isPresent()) {
            // inputId là post_id, giữ nguyên
            return inputId;
        }
        
        // inputId là quiz_pro_id, convert sang post_id
        return findPostIdByQuizProId(inputId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    /**
     * Tìm post_id từ quiz_pro_id trong wp_postmeta
     */
    private Optional<Long> findPostIdByQuizProId(Long quizProId) {
        List<PostMeta> metas = postMetaRepository.findAllByMetaKeyAndMetaValue(
                "quiz_pro_id", 
                String.valueOf(quizProId)
        );
        
        if (metas.isEmpty()) {
            return Optional.empty();
        }
        
        // Lấy post_id đầu tiên tìm được
        return Optional.of(metas.get(0).getPostId());
    }

    /**
     * Convert post_id sang quiz_pro_id (ngược lại với convertToPostId)
     * - Nếu type là QUIZ: convert post_id → quiz_pro_id
     * - Ngược lại: giữ nguyên (post_id)
     */
    private Long convertToQuizProId(Long postId, FavoritableType type) {
        if (type != FavoritableType.QUIZ) {
            return postId; // Không phải QUIZ, giữ nguyên
        }
        
        // Tìm quiz_pro_id từ post_id trong wp_postmeta
        PostMeta meta = postMetaRepository.findByPostIdAndMetaKey(postId, "quiz_pro_id");
        
        if (meta == null || meta.getMetaValue() == null) {
            // Nếu không tìm thấy quiz_pro_id, trả về post_id
            return postId;
        }
        
        try {
            return Long.parseLong(meta.getMetaValue());
        } catch (NumberFormatException e) {
            // Nếu parse lỗi, trả về post_id
            return postId;
        }
    }

    // ==================== Inner Classes ====================
    
    private record QueryResult(List<FavoriteEntity> favorites, long total) {}
    
    // ==================== Validation Methods ====================
    
    /**
     * Validate email không null/empty
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
    
    /**
     * Validate favorite ID (cho delete operations)
     */
    private void validateFavoriteId(Long favoriteId) {
        if (favoriteId == null || favoriteId <= 0) {
            throw new AppException(ErrorCode.INVALID_FAVORITE_ID);
        }
    }
    
    /**
     * Validate favoritable ID (cho add/check operations)
     */
    private void validateFavoritableId(Long favoritableId) {
        if (favoritableId == null || favoritableId <= 0) {
            throw new AppException(ErrorCode.INVALID_POST_ID);
        }
    }
    
    /**
     * Validate add favorite request
     */
    private void validateAddFavoriteRequest(AddFavoriteRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        validateFavoritableId(request.getFavoritableId());
    }
    
    /**
     * Validate favorite filter request
     */
    private void validateFavoriteFilter(FavoriteFilterRequest filter) {
        if (filter == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        
        // Validate date range
        if (filter.getFromDate() != null && filter.getToDate() != null) {
            if (filter.getFromDate().isAfter(filter.getToDate())) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
        }
        
        // Validate dates not in future
        java.time.LocalDate today = java.time.LocalDate.now();
        if (filter.getFromDate() != null && filter.getFromDate().isAfter(today)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        if (filter.getToDate() != null && filter.getToDate().isAfter(today)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        
        // Validate pagination
        if (filter.getPage() != null && filter.getPage() < 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        
        if (filter.getSize() != null) {
            if (filter.getSize() <= 0) {
                throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
            }
            if (filter.getSize() > 100) {
                throw new AppException(ErrorCode.PAGE_SIZE_TOO_LARGE);
            }
        }
        
        // Validate keyword
        if (filter.getKeyword() != null) {
            String trimmed = filter.getKeyword().trim();
            if (trimmed.isEmpty()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
            
            if (filter.getKeyword().length() > 255) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
            
            // Check for SQL injection patterns (basic)
            String keyword = filter.getKeyword();
            if (keyword.contains("--") || keyword.contains(";") || 
                keyword.toLowerCase().contains("drop ") || 
                keyword.toLowerCase().contains("delete ")) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
        }
    }
}
