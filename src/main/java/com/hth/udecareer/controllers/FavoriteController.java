package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.enums.FavoritableType;
import com.hth.udecareer.model.request.AddFavoriteRequest;
import com.hth.udecareer.model.request.FavoriteFilterRequest;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.response.FavoriteCheckResponse;
import com.hth.udecareer.model.response.FavoriteResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.service.FavoriteService;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;

/**
 * REST Controller cho Favorites Management
 * Endpoints: GET, POST, DELETE, CHECK favorites
 */
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Quản lý yêu thích")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/favorites")
    @Operation(
            summary = "Lấy danh sách favorites của user",
            description = """
                    Lấy danh sách các items yêu thích với pagination và filtering.
                    
                    **Pagination:**
                    - Không truyền page/size: Lấy tất cả
                    - Có page hoặc size: Phân trang
                    
                    **Filters:**
                    - type: COURSE, LESSON, QUIZ, TOPIC, POST
                    - fromDate/toDate: Lọc theo ngày tạo (yyyy-MM-dd)
                    - keyword: Tìm trong title, category
                    
                    **Sorting:** Mặc định theo createdAt DESC (mới nhất trước)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse getFavorites(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(required = false) FavoritableType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
                FavoriteFilterRequest filterRequest = FavoriteFilterRequest.builder()
                .type(type)
                .fromDate(fromDate)
                .toDate(toDate)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();
        
                try {
                        PageResponse<FavoriteResponse> response = favoriteService.getFavorites(
                                        principal != null ? principal.getName() : null, 
                                        filterRequest
                        );
                        return ApiResponse.success(response);
                } catch (AppException e) {
                        ErrorCode code = e.getErrorCode();
                        // Convert not found into 200 + empty page response
                        if (code == ErrorCode.POST_NOT_FOUND || code == ErrorCode.NOT_FOUND || code == ErrorCode.FAVORITE_NOT_FOUND) {
                                boolean usePagination = filterRequest.getPage() != null || filterRequest.getSize() != null;
                                if (usePagination) {
                                        int p = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
                                        int s = filterRequest.getSize() != null ? filterRequest.getSize() : 0;
                                        Pageable pageable = PageRequest.of(p, s == 0 ? 20 : s);
                                        PageResponse<FavoriteResponse> emptyPage = PageResponse.of(Collections.emptyList(), pageable, 0);
                                        return ApiResponse.success(emptyPage);
                                } else {
                                        PageResponse<FavoriteResponse> emptyPage = PageResponse.<FavoriteResponse>builder()
                                                        .content(Collections.emptyList())
                                                        .page(0)
                                                        .size(0)
                                                        .totalElements(0L)
                                                        .totalPages(0)
                                                        .hasNext(false)
                                                        .hasPrevious(false)
                                                        .first(true)
                                                        .last(true)
                                                        .build();
                                        return ApiResponse.success(emptyPage);
                                }
                        }
                        throw e;
                }
    }

    @PostMapping("/favorites")
    @Operation(
            summary = "Thêm item vào danh sách yêu thích",
            description = """
                    Thêm một item vào favorites. Hệ thống tự động phát hiện loại từ wp_posts.post_type.
                    
                    **Auto-detection:**
                    - sfwd-courses → COURSE
                    - sfwd-lessons → LESSON
                    - sfwd-quiz → QUIZ
                    - sfwd-topic → TOPIC
                    - post → POST
                    
                    **Idempotent:** Nếu đã tồn tại sẽ trả về favorite hiện tại.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse addFavorite(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody AddFavoriteRequest request
    ) {
        FavoriteResponse response = favoriteService.addFavorite(
                principal != null ? principal.getName() : null, 
                request
        );
        return ApiResponse.success(response);
    }

    @DeleteMapping("/favorites/{Id}")
    @Operation(
            summary = "Xóa favorite (soft delete)",
            description = """
                    Xóa một item khỏi danh sách yêu thích bằng ID chính của bảng.
                    
                    **Soft Delete:** Không xóa vĩnh viễn, có thể restore khi add lại.
                    **Authorization:** Chỉ user sở hữu favorite mới có thể xóa.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse deleteFavorite(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long Id
    ) {
        favoriteService.deleteFavorite(
                principal != null ? principal.getName() : null, 
                Id
        );
        return ApiResponse.success("Favorite deleted successfully");
    }



    @DeleteMapping("/favorite/{favoriteId}")
    @Operation(
            summary = "Xóa favorite theo favoriteId (soft delete)",
            description = """
                    Xóa một item khỏi danh sách yêu thích bằng favoriteId (ID của post/course/quiz được yêu thích).
                    
                    **Soft Delete:** Không xóa vĩnh viễn, có thể restore khi add lại.
                    **Authorization:** Chỉ user sở hữu favorite mới có thể xóa.
                    **Use case:** Khi bạn chỉ biết ID của item được yêu thích, không biết ID của favorite record.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse deleteFavoriteByFavoritableId(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long favoriteId
    ) {
        favoriteService.deleteFavoriteByFavoritableId(
                principal != null ? principal.getName() : null,
                favoriteId
        );
        return ApiResponse.success("Favorite deleted successfully");
    }


    @GetMapping("/favorites/check/{favoriteId}")
    @Operation(
            summary = "Kiểm tra favorite đã tồn tại chưa",
            description = """
                    Kiểm tra một item đã có trong danh sách yêu thích chưa.
                    
                    **Use case:**
                    - Hiển thị trạng thái icon yêu thích
                    - Check trước khi add/remove
                    - Sync UI state với backend
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse checkFavorite(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long favoriteId
    ) {
        FavoriteCheckResponse response = favoriteService.checkFavoriteExists(
                principal != null ? principal.getName() : null, 
                favoriteId
        );
        return ApiResponse.success(response);
    }
}
