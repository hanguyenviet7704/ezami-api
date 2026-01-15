package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.model.request.PaginationRequest;
import com.hth.udecareer.model.request.PurchaseFilterRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.PurchaseHistoryResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.PurchaseService;
import com.hth.udecareer.utils.PageableUtil;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Purchase history", description = "APIs for user purchases history and access checks")
public class PurchaseController {
    
    private final PurchaseService purchaseService;
    private final UserRepository userRepository;

    @GetMapping("purchases/history")
    @Operation(
        summary = "Tìm kiếm & lọc lịch sử mua hàng", 
        description = """
                **API THỐNG NHẤT cho tìm kiếm và lọc purchase history - HỖ TRỢ COMBINE NHIỀU FILTERS**
                
                API này cho phép bạn combine bất kỳ filters nào một cách linh hoạt:
                - Tự động lọc theo user (lấy từ JWT token)
                - Tìm kiếm theo category title (keyword)
                - Lọc theo trạng thái còn hạn/hết hạn (isActive)
                - Lọc theo khoảng thời gian mua (fromDate, toDate)
                
                **TẤT CẢ CÁC FILTERS ĐỀU LÀ TÙY CHỌN (OPTIONAL) - ngoại trừ user_id (tự động lấy từ JWT)**
                
                **Ví dụ các use cases:**
                
                1. **Lấy tất cả purchase history (không filter):**
                   GET /purchases/history?page=0&size=20
                
                2. **Tìm kiếm theo category title:**
                   GET /purchases/history?keyword=ECBA&page=0&size=20
                
                3. **Lọc purchases còn hạn:**
                   GET /purchases/history?isActive=true&page=0&size=20
                
                4. **Lọc purchases hết hạn:**
                   GET /purchases/history?isActive=false&page=0&size=20
                
                5. **Lọc theo khoảng thời gian mua:**
                   GET /purchases/history?fromDate=2024-01-01&toDate=2024-12-31&page=0&size=20
                
                6. **Combine tất cả filters:**
                   GET /purchases/history?keyword=PSM&isActive=true&fromDate=2024-01-01&toDate=2024-12-31&page=0&size=10&sort=fromTime,desc
                
                **Tham số:**
                - **keyword** (tùy chọn): Từ khóa tìm kiếm trong category title
                - **isActive** (tùy chọn): true = còn hạn, false = hết hạn, null = tất cả
                - **fromDate** (tùy chọn): Lọc purchases từ ngày này (yyyy-MM-dd)
                - **toDate** (tùy chọn): Lọc purchases đến ngày này (yyyy-MM-dd)
                - **page** (tùy chọn): Số trang (0-based), mặc định = 0
                - **size** (tùy chọn): Số records/trang, mặc định = 20, max = 100
                - **sort** (tùy chọn): Sắp xếp (vd: "fromTime,desc", "categoryCode,asc")
                
                **Response:**
                - **Có pagination (page/size)**: Trả về PageResponse với metadata đầy đủ
                - **Không pagination**: Trả về List<PurchaseHistoryResponse> (backward compatible)
                - **Lỗi 404 (PURCHASE_NOT_FOUND)**: Khi không tìm thấy purchase history nào
                
                **Lưu ý:**
                - API yêu cầu authentication (JWT token)
                - User ID tự động lấy từ JWT token
                - Date format: yyyy-MM-dd (ISO 8601)
                - Response bao gồm thông tin từ ez_quiz_category (title, imageUri)
                - Tính toán tự động: isActive, daysRemaining
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy danh sách purchase history"),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ (date range, pagination)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy purchase history", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    public Object getPurchaseHistory(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            
            @Parameter(
                    description = "Từ khóa tìm kiếm trong category title",
                    example = "ECBA",
                    required = false
            )
            @RequestParam(value = "keyword", required = false) String keyword,
            
            @Parameter(
                    description = "Lọc theo trạng thái: true = còn hạn, false = hết hạn, null = tất cả",
                    example = "true",
                    required = false
            )
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            
            @Parameter(
                    description = "Lọc purchases từ ngày này (yyyy-MM-dd)",
                    example = "2024-01-01",
                    required = false
            )
            @RequestParam(value = "fromDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(
                    description = "Lọc purchases đến ngày này (yyyy-MM-dd)",
                    example = "2024-12-31",
                    required = false
            )
            @RequestParam(value = "toDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            
            @Parameter(
                    description = "Số trang (0-based). Trang đầu tiên = 0",
                    example = "0",
                    required = false
            )
            @RequestParam(value = "page", required = false) Integer page,
            
            @Parameter(
                    description = "Số lượng records mỗi trang (1-100)",
                    example = "20",
                    required = false
            )
            @RequestParam(value = "size", required = false) Integer size,
            
            @Parameter(
                    description = "Sắp xếp theo field,direction (vd: 'fromTime,desc', 'categoryCode,asc')",
                    example = "fromTime,desc",
                    required = false
            )
            @RequestParam(value = "sort", required = false) String sort) throws Exception {
        
        // Get userId from JWT token
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        log.info("getPurchaseHistory: userId={}, keyword='{}', isActive={}, fromDate={}, toDate={}, page={}, size={}, sort={}", 
                user.getId(), keyword, isActive, fromDate, toDate, page, size, sort);
        
        PurchaseFilterRequest filter = PurchaseFilterRequest.builder()
                .userId(user.getId())
                .keyword(keyword)
                .isActive(isActive)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        
        // Check if pagination is requested (backward compatible)
        if (PageableUtil.isPaginationRequested(page, size)) {
            // Return paginated response with unified search & filter
            PaginationRequest paginationRequest = PaginationRequest.of(page, size, sort);
            return purchaseService.searchAndFilterPurchases(filter, paginationRequest.toPageable());
        } else {
            // Return full list (backward compatible with old API)
            return purchaseService.searchAndFilterPurchasesAsList(filter);
        }
    }
    
    @GetMapping("purchases/check-access/{categoryCode}")
    @Operation(
        summary = "Kiểm tra quyền truy cập category",
        description = "Kiểm tra xem user có quyền truy cập vào một category cụ thể hay không",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kiểm tra thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    public Boolean checkAccess(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @Parameter(description = "Category code cần kiểm tra", example = "ecba") 
            @PathVariable String categoryCode) {
        boolean hasAccess = purchaseService.hasAccess(principal, categoryCode);
        log.info("User {} access to category {}: {}", principal.getName(), categoryCode, hasAccess);
        return hasAccess;
    }
}