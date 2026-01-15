package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.*;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserRepository userRepository;

    @Operation(
            tags = {"Feed"},
            summary = "Lấy danh sách bài viết (feeds)",
            description = """
                    **Lấy danh sách bài viết với phân trang và các bộ lọc (TẤT CẢ THAM SỐ ĐỀU LÀ OPTIONAL)**
                    
                    **Tất cả các tham số dưới đây đều là OPTIONAL - có thể bỏ qua hoặc combine nhiều filters:**
                    
                    **Request Parameters (Tất cả đều optional):**
                    - `page`: Số trang (mặc định: 1) - **OPTIONAL**
                    - `per_page`: Số bài viết mỗi trang (mặc định: 10) - **OPTIONAL**
                    - `space`: Slug của space để lọc bài viết - **OPTIONAL**
                    - `user_id`: User ID để lọc bài viết của user - **OPTIONAL**
                    - `search`: Từ khóa tìm kiếm - **OPTIONAL**
                    - `search_in`: Các trường tìm kiếm (mặc định: ['post_content']) - **OPTIONAL**
                    - `order_by_type`: Sắp xếp theo (mặc định: 'latest') - **OPTIONAL**
                      
                      **Các giá trị hợp lệ:**
                      - `latest` hoặc `created_at_desc` hoặc `newest`: Mới nhất trước (mặc định)
                      - `oldest` hoặc `created_at_asc`: Cũ nhất trước
                      - `most_commented` hoặc `comments_desc`: Nhiều comment nhất
                      - `most_reactions` hoặc `reactions_desc` hoặc `most_liked`: Nhiều like/reaction nhất
                      - `trending`: Đang trending (dựa trên reactions và thời gian)
                    - `disable_sticky`: Tắt sticky posts ('yes' hoặc '') - **OPTIONAL**
                    
                    **Ví dụ sử dụng:**
                    - Lấy tất cả: `GET /api/feeds`
                    - Lọc theo space: `GET /api/feeds?space=start-here`
                    - Lọc theo user: `GET /api/feeds?user_id=1`
                    - Tìm kiếm: `GET /api/feeds?search=đẹp`
                    - Sắp xếp: `GET /api/feeds?order_by_type=most_reactions`
                    - Combine nhiều filters: `GET /api/feeds?space=start-here&user_id=1&search=đẹp&order_by_type=trending`
                    
                    **Response:** Danh sách bài viết với pagination, comments, reactions, và thông tin user
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @GetMapping("/feeds")
    public ResponseEntity<FeedListResponse> listFeeds(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Số trang (mặc định: 1)", example = "1")
            @RequestParam(value = "page", required = false) Integer page,
            @Parameter(description = "Số bài viết mỗi trang (mặc định: 10)", example = "10")
            @RequestParam(value = "per_page", required = false) Integer perPage,
            @Parameter(description = "Slug của space để lọc bài viết", example = "start-here")
            @RequestParam(value = "space", required = false) String space,
            @Parameter(description = "User ID để lọc bài viết của user", example = "1")
            @RequestParam(value = "user_id", required = false) Long userId,
            @Parameter(description = "Từ khóa tìm kiếm", example = "đẹp")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Các trường tìm kiếm (mặc định: ['post_content'])", example = "['post_content']")
            @RequestParam(value = "search_in", required = false) java.util.List<String> searchIn,
            @Parameter(description = "Sắp xếp theo: 'latest' (mặc định), 'oldest', 'most_commented', 'most_reactions', 'trending'", example = "latest")
            @RequestParam(value = "order_by_type", required = false) String orderByType,
            @Parameter(description = "Tắt sticky posts ('yes' hoặc '')", example = "")
            @RequestParam(value = "disable_sticky", required = false) String disableSticky) throws AppException {
        
        // Build FeedListRequest from individual parameters
        FeedListRequest request = FeedListRequest.builder()
                .page(page)
                .perPage(perPage)
                .space(space)
                .userId(userId)
                .search(search)
                .searchIn(searchIn)
                .orderByType(orderByType)
                .disableSticky(disableSticky)
                .build();
        
        Long currentUserId = getUserIdFromPrincipal(principal, false);
        FeedListResponse response = feedService.listFeeds(request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Lấy bài viết theo slug",
            description = """
                    **Lấy thông tin chi tiết của một bài viết theo slug**
                    
                    **Request Parameters:**
                    - `feed_slug`: Slug của bài viết (URL path)
                    - `context`: Context ('edit' để lấy dữ liệu edit)
                    
                    **Response:** Thông tin chi tiết bài viết
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy bài viết thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết")
    })
    @GetMapping("/feeds/{feed_slug}/by-slug")
    public ResponseEntity<FeedDetailResponse> getFeedBySlug(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_slug") String feedSlug,
            @RequestParam(value = "context", required = false) String context) throws AppException {
        
        Long currentUserId = getUserIdFromPrincipal(principal, false);
        FeedDetailResponse response = feedService.getFeedBySlug(feedSlug, context, currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Lấy bài viết theo ID",
            description = """
                    **Lấy thông tin chi tiết của một bài viết theo ID**
                    
                    **Request Parameters:**
                    - `feed_id`: ID của bài viết
                    
                    **Response:** Thông tin chi tiết bài viết
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy bài viết thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết")
    })
    @GetMapping({"/feeds/{feed_id}/by-id", "/feeds/{feed_id}"})
    public ResponseEntity<FeedDetailResponse> getFeedById(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId) throws AppException {

        Long currentUserId = getUserIdFromPrincipal(principal, false);
        FeedDetailResponse response = feedService.getFeedById(feedId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Tạo bài viết mới",
            description = """
                    **Tạo một bài viết mới trong cộng đồng**
                    
                    **Request Body:**
                    - `message`: Nội dung bài viết (required)
                    - `space`: Slug của space (optional)
                    - `title`: Tiêu đề (optional)
                    - `content_type`: Loại nội dung (text, image, video)
                    - `privacy`: Quyền riêng tư (public, private)
                    - `topic_ids`: Danh sách topic IDs (optional)
                    - `media_items`: Danh sách media items với media_key (optional)
                    
                    **Response:** Thông tin bài viết vừa tạo
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo bài viết thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds")
    public ResponseEntity<FeedCreateResponse> createFeed(
            @Parameter(hidden = true) Principal principal,
            @RequestBody @Valid FeedCreateRequest request) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        FeedCreateResponse response = feedService.createFeed(request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Cập nhật bài viết",
            description = """
                    **Cập nhật toàn bộ thông tin của một bài viết**
                    
                    **Request Body:** Giống như POST /feeds
                    
                    **Response:** Thông tin bài viết đã cập nhật
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}")
    public ResponseEntity<FeedCreateResponse> updateFeed(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId,
            @RequestBody @Valid FeedUpdateRequest request) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        FeedCreateResponse response = feedService.updateFeed(feedId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Patch bài viết (cập nhật một phần)",
            description = """
                    **Cập nhật một phần thông tin của bài viết (is_sticky)**
                    
                    **Request Body:**
                    - `is_sticky`: 0 hoặc 1 - Đánh dấu bài viết sticky (luôn hiển thị đầu tiên)
                    
                    **Thứ tự sắp xếp bài viết:**
                    1. **is_sticky = 1** → Hiển thị đầu tiên
                    2. **created_at** → Bài viết mới hơn hiển thị trước
                    
                    **Response:** Thông tin bài viết đã cập nhật
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/feeds/{feed_id}")
    public ResponseEntity<FeedCreateResponse> patchFeed(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId,
            @RequestBody @Valid FeedPatchRequest request) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        FeedCreateResponse response = feedService.patchFeed(feedId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Xóa mềm bài viết",
            description = """
                    **Xóa mềm (soft delete) một bài viết**
                    
                    **Cách hoạt động:**
                    - Thay đổi status của feed thành "deleted"
                    - Feed vẫn tồn tại trong database nhưng không hiển thị trong danh sách
                    - Chỉ owner mới có thể xóa feed của mình
                    
                    **Response:** 
                    - Feed đã được xóa mềm với status = "deleted"
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedCreateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feed không tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}/delete")
    public ResponseEntity<FeedCreateResponse> softDeleteFeed(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        FeedCreateResponse response = feedService.softDeleteFeed(feedId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Feed"},
            summary = "Bookmark/Unbookmark bài viết",
            description = """
                    **Bookmark hoặc Unbookmark một bài viết (feed)**

                    **Cách hoạt động:**
                    - Nếu chưa bookmark → Tạo bookmark
                    - Nếu đã bookmark → Xóa bookmark

                    **Response:**
                    - `feed_id`: ID của bài viết
                    - `bookmarked`: true nếu đã bookmark, false nếu đã unbookmark
                    - `message`: Thông báo kết quả
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedBookmarkResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feed không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}/bookmark")
    public ResponseEntity<FeedBookmarkResponse> toggleBookmark(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal, true);
        FeedBookmarkResponse response = feedService.toggleBookmark(feedId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Reaction"},
            summary = "Like/Unlike bài viết",
            description = """
                    **Like hoặc Unlike một bài viết (feed)**

                    **Cách hoạt động:**
                    - Nếu chưa like → Tạo reaction (like)
                    - Nếu đã like → Xóa reaction (unlike)

                    **Response:**
                    - `feed_id`: ID của bài viết
                    - `liked`: true nếu đã like, false nếu đã unlike
                    - `reactions_count`: Tổng số reactions của bài viết
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedReactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feed không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}/reaction")
    public ResponseEntity<FeedReactionResponse> toggleReaction(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal, true);
        FeedReactionResponse response = feedService.toggleReaction(feedId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Reaction"},
            summary = "Kiểm tra trạng thái like của bài viết",
            description = """
                    **Kiểm tra xem user hiện tại đã like feed hay chưa**
                    
                    **Use case:**
                    - Dùng để hiển thị trạng thái like khi load trang
                    - Dùng để sync UI với database
                    - Dùng để check trước khi toggle
                    
                    **Response:**
                    - `feedId`: ID của feed
                    - `liked`: true nếu đã like, false nếu chưa like
                    - `reactionsCount`: Tổng số lượt like của feed
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedReactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feed không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/feeds/{feed_id}/reaction/status")
    public ResponseEntity<FeedReactionResponse> checkReactionStatus(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "ID của feed cần kiểm tra", required = true)
            @PathVariable("feed_id") Long feedId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal, true);
        FeedReactionResponse response = feedService.checkReactionStatus(feedId, userId);
        return ResponseEntity.ok(response);
    }

    // ============= COMMENTS API =============

    @Operation(
            tags = {"Comment"},
            summary = "Lấy danh sách comments của feed",
            description = """
                    **Lấy danh sách comments của một feed với phân trang**
                    
                    **Request Parameters:**
                    - `feed_id`: ID của feed (URL path)
                    - `page`: Số trang (mặc định: 1)
                    - `per_page`: Số comments mỗi trang (mặc định: 20)
                    - `order_by`: Sắp xếp (`created_at_asc` - mặc định, `created_at_desc`)
                    - `comment_id`: ID của comment cha (optional) - Nếu có thì lấy comment con của comment cha, nếu không có thì lấy root comments
                    
                    **Response:** Danh sách comments với pagination
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentListResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feed hoặc comment không tồn tại")
    })
    @GetMapping("/feeds/{feed_id}/comments")
    public ResponseEntity<CommentListResponse> getFeedComments(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId,
            @Parameter(description = "Số trang (mặc định: 1)", example = "1")
            @RequestParam(value = "page", required = false) Integer page,
            @Parameter(description = "Số comments mỗi trang (mặc định: 20)", example = "20")
            @RequestParam(value = "per_page", required = false) Integer perPage,
            @Parameter(description = "Sắp xếp (created_at_asc hoặc created_at_desc)", example = "created_at_asc")
            @RequestParam(value = "order_by", required = false) String orderBy,
            @Parameter(description = "ID của comment cha (optional) - Nếu có thì lấy comment con, nếu không có thì lấy root comments", example = "123")
            @RequestParam(value = "comment_id", required = false) Long commentId) throws AppException {
        
        Long currentUserId = getUserIdFromPrincipal(principal, false);
        CommentListResponse response = feedService.getFeedComments(feedId, page, perPage, orderBy, commentId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Comment"},
            summary = "Tạo comment mới hoặc reply comment",
            description = """
                    **Tạo comment mới hoặc reply vào một comment**
                    
                    **Cách sử dụng:**
                    - **Tạo comment gốc:** Không có `parent_id` hoặc `parent_id = null`
                    - **Reply comment:** Có `parent_id` trỏ đến ID của comment cha
                    
                    **Request Body:**
                    - `message` (required): Nội dung comment
                    - `parent_id` (optional): ID comment cha để reply. Nếu null hoặc không có, đây là comment gốc
                    - `media_images` (optional): Mảng các media items với `media_key` (từ upload media API). Nếu null hoặc rỗng, không có ảnh đính kèm
                    
                    **Ví dụ Request Body:**
                    ```json
                    {
                      "message": "Đây là comment của tôi",
                      "parent_id": null,
                      "media_images": [
                        {
                          "media_key": "abc123-def456-ghi789"
                        },
                        {
                          "media_key": "xyz789-uvw456-rst123"
                        }
                      ]
                    }
                    ```
                    
                    **Lưu ý:**
                    - Chỉ có thể reply vào comment đã published và thuộc cùng feed
                    - Media keys phải là các key hợp lệ từ upload media API
                    - Comment sẽ tự động được set status = "published"
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo comment thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Feed hoặc parent comment không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}/comments")
    public ResponseEntity<CommentDetailResponse> createComment(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId,
            @RequestBody @Valid CommentCreateRequest request) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        CommentDetailResponse response = feedService.createComment(feedId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Comment"},
            summary = "Cập nhật comment",
            description = """
                    **Cập nhật nội dung và/hoặc media của một comment**
                    
                    **Quyền hạn:**
                    - Chỉ owner của comment mới có thể cập nhật
                    - Comment phải có status = "published"
                    
                    **Request Body:**
                    - `message` (required): Nội dung comment đã sửa
                    - `media_images` (optional): Cách xử lý media:
                      - Nếu `null` hoặc không gửi: Giữ nguyên media cũ
                      - Nếu `[]` (mảng rỗng): Xóa tất cả media cũ
                      - Nếu có giá trị: Thay thế toàn bộ media cũ bằng media mới
                    
                    **Ví dụ Request Body:**
                    ```json
                    {
                      "message": "Nội dung comment đã được sửa",
                      "media_images": [
                        {
                          "media_key": "new-abc123-def456"
                        },
                        {
                          "media_key": "new-xyz789-uvw456"
                        }
                      ]
                    }
                    ```
                    
                    **Lưu ý:**
                    - Media cũ sẽ bị xóa hoàn toàn khi gửi `media_images` mới
                    - Nếu muốn giữ media cũ, không gửi field `media_images` hoặc gửi `null`
                    - Media keys phải là các key hợp lệ từ upload media API
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Comment không tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}/comments/{comment_id}")
    public ResponseEntity<CommentDetailResponse> updateComment(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId,
            @PathVariable("comment_id") Long commentId,
            @RequestBody @Valid CommentUpdateRequest request) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        CommentDetailResponse response = feedService.updateComment(feedId, commentId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Comment"},
            summary = "Xóa mềm comment",
            description = """
                    **Xóa mềm (soft delete) một comment**
                    
                    **Cách hoạt động:**
                    - Thay đổi status của comment thành "deleted"
                    - Comment vẫn tồn tại trong database nhưng không hiển thị trong danh sách
                    - Chỉ owner mới có thể xóa comment của mình
                    
                    **Response:** 
                    - Comment đã được xóa mềm với status = "deleted"
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment không tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/feeds/{feed_id}/comments/{comment_id}/delete")
    public ResponseEntity<CommentDetailResponse> softDeleteComment(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("feed_id") Long feedId,
            @PathVariable("comment_id") Long commentId) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        CommentDetailResponse response = feedService.softDeleteComment(feedId, commentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Reaction"},
            summary = "Like/Unlike comment",
            description = """
                    **Like hoặc Unlike một comment**
                    
                    **Cách hoạt động:**
                    - Nếu chưa like → Tạo reaction (like)
                    - Nếu đã like → Xóa reaction (unlike)
                    
                    **Response:** 
                    - `comment_id`: ID của comment
                    - `liked`: true nếu đã like, false nếu đã unlike
                    - `reactions_count`: Tổng số reactions của comment
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentReactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/comments/{comment_id}/reaction")
    public ResponseEntity<CommentReactionResponse> toggleCommentReaction(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("comment_id") Long commentId) throws AppException {
        
        Long userId = getUserIdFromPrincipal(principal, true);
        CommentReactionResponse response = feedService.toggleCommentReaction(commentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            tags = {"Comment"},
            summary = "Lấy comment theo ID",
            description = """
                    **Lấy thông tin chi tiết của một comment theo ID**
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy comment thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment không tồn tại")
    })
    @GetMapping("/commentsFeed/{id}")
    public ResponseEntity<CommentResponse> getCommentById(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("id") Long commentId) throws AppException {
        
        Long currentUserId = getUserIdFromPrincipal(principal, false);
        CommentResponse response = feedService.getCommentById(commentId, currentUserId);
        return ResponseEntity.ok(response);
    }

    // ============= PRIVATE HELPER METHODS =============

    private Long getUserIdFromPrincipal(Principal principal, boolean required) throws AppException {
        if (principal == null) {
            if (required) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return null;
        }

        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            if (required) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return null;
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null || user.getId() == null) {
            if (required) {
                throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);
            }
            return null;
        }

        return user.getId();
    }

    
}

