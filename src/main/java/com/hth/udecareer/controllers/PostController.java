package com.hth.udecareer.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.PaginationRequest;
import com.hth.udecareer.model.request.PostFilterRequest;
import com.hth.udecareer.model.response.ArticleSpaceResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.PostResponse;
import com.hth.udecareer.service.PostService;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import java.util.Collections;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import com.hth.udecareer.utils.PageableUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Post (Article) Public API")
public class PostController {
    private final PostService postService;

    @Operation(
            summary = "Tìm kiếm & lọc bài viết", 
            description = """
                    **API THỐNG NHẤT cho tìm kiếm và lọc bài viết - HỖ TRỢ COMBINE NHIỀU FILTERS**
                    
                    API này cho phép bạn combine bất kỳ filters nào một cách linh hoạt:
                    - Tìm kiếm theo từ khóa (keyword)
                    - Lọc theo danh mục (categoryIds - có thể truyền nhiều IDs)
                    - Lọc theo tác giả (authorId hoặc authorUsername)
                    - Lọc theo tags
                    - Lọc theo khoảng thời gian (fromDate, toDate)
                    
                    **TẤT CẢ CÁC FILTERS ĐỀU LÀ TÙY CHỌN (OPTIONAL)**
                    
                    **Ví dụ các use cases:**
                    
                    1. **Lấy tất cả bài viết (không filter):**
                       GET /post?page=0&size=20
                    
                    2. **Tìm kiếm theo keyword:**
                       GET /post?keyword=wordpress&page=0&size=20
                    
                    3. **Lọc theo 1 category:**
                       GET /post?categoryIds=5&page=0&size=20
                    
                    4. **Lọc theo NHIỀU categories:**
                       GET /post?categoryIds=5,10,15&page=0&size=20
                    
                    5. **Search + Filter categories:**
                       GET /post?keyword=wordpress&categoryIds=5,10&page=0&size=20
                    
                    6. **Search + Categories + Author + Tags + DateRange:**
                       GET /post?keyword=tutorial&categoryIds=5,10&authorId=1&tags=beginner,php&fromDate=2024-01-01&toDate=2024-12-31&page=0&size=10&sort=date,desc
                    
                    7. **Lọc theo tác giả:**
                       GET /post?authorId=1&page=0&size=20
                    
                    8. **Lọc theo tags:**
                       GET /post?tags=wordpress,tutorial,beginner&page=0&size=20
                    
                    9. **Lọc theo khoảng thời gian:**
                       GET /post?fromDate=2024-01-01&toDate=2024-03-31&page=0&size=20
                    
                    **Tham số:**
                    - **keyword** (tùy chọn): Từ khóa tìm kiếm trong title/content
                    - **categoryIds** (tùy chọn): Danh sách IDs danh mục (phân cách bằng dấu phẩy)
                    - **authorId** (tùy chọn): ID tác giả
                    - **tags** (tùy chọn): Danh sách tags (phân cách bằng dấu phẩy)
                    - **fromDate** (tùy chọn): Ngày bắt đầu (yyyy-MM-dd)
                    - **toDate** (tùy chọn): Ngày kết thúc (yyyy-MM-dd)
                    - **page** (tùy chọn): Số trang (0-based), mặc định = 0
                    - **size** (tùy chọn): Số bài viết/trang, mặc định = 20, max = 100
                    - **sort** (tùy chọn): Sắp xếp (vd: "date,desc", "title,asc")
                    
                    **Response:**
                    - **Có pagination (page/size)**: Trả về PageResponse với metadata đầy đủ
                    - **Không pagination**: Trả về List<PostResponse> (backward compatible)
                    - Chỉ trả về bài đã publish (PUBLISH/FUTURE/PRIVATE)
                    - **Lỗi 404 (POST_NOT_FOUND)**: Khi không tìm thấy bài viết nào
                    
                    **Lưu ý:**
                    - API public, không yêu cầu authentication
                    - Tất cả filters có thể combine tự do
                    - Date format: yyyy-MM-dd (ISO 8601)
                    - Tags/CategoryIds phân cách bởi dấu phẩy, không có khoảng trắng
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy danh sách bài viết"),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ (date range, pagination)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @GetMapping("/post")
    public Object searchPost(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Từ khóa tìm kiếm trong title hoặc content",
                    example = "wordpress",
                    required = false
            )
            @RequestParam(value = "keyword", required = false) String keyword,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Danh sách IDs của categories cần lọc (phân cách bởi dấu phẩy). Ví dụ: 5,10,15",
                    example = "5,10,15",
                    required = false
            )
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "ID của tác giả",
                    example = "1",
                    required = false
            )
            @RequestParam(value = "authorId", required = false) Long authorId,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Danh sách tags (phân cách bởi dấu phẩy). Ví dụ: beginner,wordpress,tutorial",
                    example = "wordpress,tutorial",
                    required = false
            )
            @RequestParam(value = "tags", required = false) List<String> tags,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Ngày bắt đầu lọc (yyyy-MM-dd)",
                    example = "2024-01-01",
                    required = false
            )
            @RequestParam(value = "fromDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Ngày kết thúc lọc (yyyy-MM-dd)",
                    example = "2024-12-31",
                    required = false
            )
            @RequestParam(value = "toDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số trang (0-based). Trang đầu tiên = 0",
                    example = "0",
                    required = false
            )
            @RequestParam(value = "page", required = false) Integer page,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số lượng bài viết mỗi trang (1-100)",
                    example = "20",
                    required = false
            )
            @RequestParam(value = "size", required = false) Integer size,
            
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Sắp xếp theo field,direction (vd: 'date,desc', 'title,asc')",
                    example = "date,desc",
                    required = false
            )
            @RequestParam(value = "sort", required = false) String sort) throws Exception {
        
        log.info("searchPost: keyword='{}', categoryIds={}, authorId={}, tags={}, fromDate={}, toDate={}, page={}, size={}, sort={}", 
                keyword, categoryIds, authorId, tags, fromDate, toDate, page, size, sort);
        
        PostFilterRequest filter = PostFilterRequest.builder()
                .keyword(keyword)
                .categoryIds(categoryIds)
                .authorId(authorId)
                .tags(tags)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        
                // Check if pagination is requested (backward compatible)
                if (PageableUtil.isPaginationRequested(page, size)) {
                        // Return paginated response with unified search & filter
                        PaginationRequest paginationRequest = PaginationRequest.of(page, size, sort);
                        try {
                                return postService.searchAndFilterPosts(filter, paginationRequest.toPageable());
                        } catch (AppException e) {
                                if (e.getErrorCode() == ErrorCode.POST_NOT_FOUND) {
                                        Pageable pageable = paginationRequest.toPageable();
                                        return PageResponse.of(Collections.emptyList(), pageable, 0);
                                }
                                throw e;
                        }
                } else {
                        // Return full list (backward compatible with old API)
                        try {
                                return postService.searchAndFilterPostsAsList(filter);
                        } catch (AppException e) {
                                if (e.getErrorCode() == ErrorCode.POST_NOT_FOUND) {
                                        return Collections.emptyList();
                                }
                                throw e;
                        }
                }
    }

    @Operation(
            summary = "Lấy danh sách Article Spaces", 
            description = """
                    **Lấy danh sách các Article Spaces (không gian bài viết)**
                    
                    Article Space là một tập hợp các bài viết được nhóm lại theo một chủ đề/topic cụ thể.
                    
                    **Tham số:**
                    - **language** (tùy chọn): Mã ngôn ngữ (vd: "vn", "en"), mặc định "vn"
                    - **appCode** (tùy chọn): Mã ứng dụng, mặc định "ezami"
                    - **withCategory** (tùy chọn): Include categories hay không, mặc định true
                    
                    **Response:**
                    - Danh sách article spaces với metadata
                    - Categories trong space (nếu withCategory = true)
                    - Số lượng bài viết trong mỗi space/category
                    
                    **Use cases:**
                    - Hiển thị danh sách chủ đề bài viết
                    - Navigation/menu cho blog section
                    - Multi-language content organization
                    
                    **Lưu ý:**
                    - API này KHÔNG yêu cầu authentication (public)
                    - Hỗ trợ đa ngôn ngữ và multi-app
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách article spaces thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @GetMapping("/post/space")
    public List<ArticleSpaceResponse> searchPostSpace(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Mã ngôn ngữ (vd: 'vn', 'en')",
                    example = "vn",
                    required = false
            )
            @RequestParam(value = "language", required = false) String language,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Mã ứng dụng",
                    example = "ezami",
                    required = false
            )
            @RequestParam(value = "appCode", required = false) String appCode,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Có bao gồm categories trong response hay không",
                    example = "true",
                    required = false
            )
            @RequestParam(value = "withCategory", required = false) Boolean withCategory) {
        log.info("searchPostSpace: appCode {}, language {}", appCode, language);
        return postService.findAllSpace(Optional.ofNullable(appCode).orElse("ezami"),
                Optional.ofNullable(language).orElse("vn"),
                Optional.ofNullable(withCategory).orElse(true));
    }

    @Operation(
            summary = "Lấy chi tiết một Article Space", 
            description = """
                    **Lấy thông tin chi tiết của một Article Space cụ thể**
                    
                    API này trả về thông tin đầy đủ của một article space theo ID.
                    
                    **Tham số:**
                    - **spaceId** (bắt buộc): ID của article space cần lấy
                    - **language** (tùy chọn): Mã ngôn ngữ, mặc định "vn"
                    
                    **Response:**
                    - Thông tin chi tiết article space
                    - Danh sách categories trong space
                    - Metadata và cấu hình
                    
                    **Use cases:**
                    - Hiển thị chi tiết một chủ đề bài viết
                    - Load categories để filter bài viết
                    - Navigation breadcrumb
                    
                    **Lưu ý:**
                    - API này KHÔNG yêu cầu authentication (public)
                    - Throw 404 nếu không tìm thấy space
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy article space"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy article space", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @GetMapping("/post/space/{spaceId}")
    public ArticleSpaceResponse getPostSpace(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "ID của article space cần lấy",
                    required = true,
                    example = "10"
            )
            @PathVariable("spaceId") Long spaceId,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Mã ngôn ngữ (vd: 'vn', 'en')",
                    example = "vn",
                    required = false
            )
            @RequestParam(value = "language", required = false) String language) throws Exception {
        log.info("getPostSpace: spaceId {}, language {}", spaceId, language);
        return postService.getSpace(spaceId, Optional.ofNullable(language).orElse("vn"));
    }

    @Operation(
            summary = "Lấy chi tiết bài viết theo ID", 
            description = """
                    **Lấy thông tin đầy đủ của một bài viết cụ thể theo ID**
                    
                    API này trả về toàn bộ nội dung của một bài viết theo ID.
                    
                    **Tham số:**
                    - **postId** (bắt buộc): ID của bài viết cần lấy
                    
                    **Response:**
                    - ID, title, name (slug)
                    - **Full content** (HTML) của bài viết
                    - Status (PUBLISH, DRAFT, PRIVATE)
                    - Type (post, page, etc.)
                    
                    **Use cases:**
                    - Hiển thị trang đọc bài viết đầy đủ
                    - Preview bài viết trong editor
                    - Share link bài viết cụ thể
                    
                    **Lưu ý:**
                    - API này KHÔNG yêu cầu authentication (public)
                    - Chỉ trả về bài có status = PUBLISH
                    - Content là HTML, cần render đúng cách ở client
                    - Throw 404 nếu không tìm thấy bài viết
                    - Có cache 10 phút (Redis)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy bài viết"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
        @GetMapping("/post/{postId}")
        public Object getPostInfo(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "ID của bài viết cần lấy",
                    required = true,
                    example = "42"
            )
                        @PathVariable("postId") Long postId) throws Exception {
                log.info("getPostInfo: postId {}", postId);
                try {
                        return postService.findById(postId);
                } catch (com.hth.udecareer.exception.AppException e) {
                        if (e.getErrorCode() == ErrorCode.POST_NOT_FOUND) {
                                // Return 200 + empty array instead of 404
                                return Collections.emptyList();
                        }
                        throw e;
                }
    }

    @Operation(
            summary = "Tìm kiếm bài viết theo từ khóa",
            description = """
                    **Tìm kiếm bài viết theo keyword trong title hoặc content**
                    
                    API này thực hiện full-text search trên title và content của bài viết.
                    
                    **Tham số:**
                    - **keyword** (bắt buộc): Từ khóa cần tìm
                    - **page** (tùy chọn): Số trang (0-based), mặc định = 0
                    - **size** (tùy chọn): Số bài viết mỗi trang, mặc định = 20, tối đa = 100
                    - **sort** (tùy chọn): Sắp xếp theo field (vd: "date,desc", "title,asc")
                    
                    **Response:**
                    - Danh sách bài viết match với keyword
                    - Metadata phân trang đầy đủ
                    - Chỉ trả về bài đã publish
                    
                    **Search Logic:**
                    - Case-insensitive search
                    - Tìm trong cả title VÀ content
                    - Hỗ trợ partial matching (contains)
                    
                    **Use cases:**
                    - Search box trên blog
                    - Filter kết quả theo keyword
                    - SEO-friendly search results
                    
                    **Lưu ý:**
                    - Keyword phải có ít nhất 1 ký tự
                    - Kết quả được sắp xếp theo relevance (có thể customize)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "400", description = "Keyword trống hoặc tham số không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    @GetMapping("/post/search")
    public PageResponse<PostResponse> searchPostsByKeyword(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Từ khóa cần tìm kiếm",
                    example = "wordpress",
                    required = true
            )
            @RequestParam(value = "keyword") String keyword,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số trang (0-based)",
                    example = "0",
                    required = false
            )
            @RequestParam(value = "page", required = false) Integer page,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số lượng bài viết mỗi trang (1-100)",
                    example = "20",
                    required = false
            )
            @RequestParam(value = "size", required = false) Integer size,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Sắp xếp theo field,direction",
                    example = "date,desc",
                    required = false
            )
            @RequestParam(value = "sort", required = false) String sort) {
        log.info("searchPostsByKeyword: keyword='{}', page={}, size={}", keyword, page, size);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }
        
                PaginationRequest paginationRequest = PaginationRequest.of(page, size, sort);
                try {
                        return postService.searchPosts(keyword, paginationRequest.toPageable());
                } catch (AppException e) {
                        if (e.getErrorCode() == ErrorCode.POST_NOT_FOUND) {
                                return PageResponse.of(Collections.emptyList(), paginationRequest.toPageable(), 0);
                        }
                        throw e;
                }
    }

    @Operation(
            summary = "Lấy bài viết theo tác giả",
            description = """
                    **Lấy danh sách bài viết của một tác giả cụ thể**
                    
                    API này trả về tất cả bài viết được viết bởi một tác giả.
                    
                    **Tham số:**
                    - **authorId** (tùy chọn): ID của tác giả
                    - **authorUsername** (tùy chọn): Username của tác giả
                    - **page** (tùy chọn): Số trang, mặc định = 0
                    - **size** (tùy chọn): Số bài viết mỗi trang, mặc định = 20
                    - **sort** (tùy chọn): Sắp xếp (vd: "date,desc")
                    
                    **Response:**
                    - Danh sách bài viết của tác giả
                    - Metadata phân trang
                    
                    **Use cases:**
                    - Trang "All posts by Author X"
                    - Author profile page
                    - Filter by author
                    
                    **Lưu ý:**
                    - Phải truyền ít nhất authorId HOẶC authorUsername
                    - Nếu truyền cả 2, ưu tiên authorId
                    - Chỉ trả về bài đã publish
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "400", description = "Thiếu tham số authorId hoặc authorUsername", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    @GetMapping("/post/author")
    public PageResponse<PostResponse> getPostsByAuthor(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "ID của tác giả",
                    example = "1",
                    required = false
            )
            @RequestParam(value = "authorId", required = false) Long authorId,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Username của tác giả",
                    example = "admin",
                    required = false
            )
            @RequestParam(value = "authorUsername", required = false) String authorUsername,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số trang (0-based)",
                    example = "0",
                    required = false
            )
            @RequestParam(value = "page", required = false) Integer page,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số lượng bài viết mỗi trang",
                    example = "20",
                    required = false
            )
            @RequestParam(value = "size", required = false) Integer size,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Sắp xếp theo field,direction",
                    example = "date,desc",
                    required = false
            )
            @RequestParam(value = "sort", required = false) String sort) {
        log.info("getPostsByAuthor: authorId={}, authorUsername='{}', page={}, size={}", 
                authorId, authorUsername, page, size);
        
        if (authorId == null && (authorUsername == null || authorUsername.trim().isEmpty())) {
            throw new IllegalArgumentException("Either authorId or authorUsername must be provided");
        }
        
                PaginationRequest paginationRequest = PaginationRequest.of(page, size, sort);
        
                try {
                        // Ưu tiên authorId nếu có cả 2
                        if (authorId != null) {
                                return postService.findPostsByAuthorId(authorId, paginationRequest.toPageable());
                        } else {
                                return postService.findPostsByAuthor(authorUsername, paginationRequest.toPageable());
                        }
                } catch (AppException e) {
                        if (e.getErrorCode() == ErrorCode.POST_NOT_FOUND) {
                                return PageResponse.of(Collections.emptyList(), paginationRequest.toPageable(), 0);
                        }
                        throw e;
                }
    }
}