package com.hth.udecareer.controllers;


import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.SpaceMembershipResponse;
import com.hth.udecareer.model.response.SpaceResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.SpaceService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Community Spaces Public API", description = "API quản lý các không gian cộng đồng (Spaces/Groups)")
public class SpaceController {
    private final SpaceService spaceService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Lấy danh sách tất cả Spaces (Menu Tree)",
            description = """
                    **Lấy toàn bộ danh sách Spaces theo cấu trúc phân cấp (Tree Structure)**
                    
                    API này trả về danh sách các Space Group (nhóm) và các Community Space (con) bên trong.
                    Dữ liệu được tổ chức dạng cây (nested JSON) để Frontend dễ dàng render thành Menu bên trái.
                    
                    **Logic lọc:**
                    - Chỉ trả về các space có `privacy = public`
                    - Chỉ trả về các space có `status = published` hoặc `active`
                    - Tự động lồng ghép Space con vào trong Space cha
                    
                    **Use cases:**
                    - Hiển thị Sidebar Menu / Navigation
                    - Lấy danh sách cộng đồng để user chọn tham gia
                    
                    **Lưu ý:**
                    - API này KHÔNG yêu cầu authentication (public)
                    - Response trả về là List các Root Space (Space cha)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @GetMapping("/spaces")
    public List<SpaceResponse> getSpaces() {
        return spaceService.getSpaces();
    }


    @Operation(
            summary = "Lấy chi tiết Space theo Slug",
            description = """
                    **Lấy thông tin chi tiết của một Space dựa trên URL Slug**
                    
                    API này dùng để lấy thông tin metadata của Space khi user truy cập vào đường dẫn (URL).
                    
                    **Tham số:**
                    - **slug** (bắt buộc): URL thân thiện của space (vd: "start-here", "general-chat")
                    
                    **Response:**
                    - Thông tin chi tiết: ID, Title, Description, Privacy, Type
                    - Settings (nếu có)
                    
                    **Use cases:**
                    - Render header của trang Space
                    - Kiểm tra quyền truy cập (Privacy Check) trước khi load bài viết
                    - SEO metadata
                    
                    **Lỗi thường gặp:**
                    - 404 Not Found: Nếu slug không tồn tại trong hệ thống
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy Space"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Space (Sai slug)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    @GetMapping("/spaces/{slug}/by-slug")
    public SpaceResponse getSpaces(@PathVariable String slug) {
        return spaceService.getSpaceBySlug(slug);
    }



    @Operation(
            summary = "Lấy chi tiết Space theo ID",
            description = """
                    **Lấy thông tin chi tiết của một Space dựa trên ID**
                    
                    API này tương tự như lấy theo slug nhưng dùng ID số nguyên.
                    Thường dùng cho các thao tác nội bộ hoặc khi Frontend đã có sẵn ID.
                    
                    **Tham số:**
                    - **spaceId** (bắt buộc): ID số của space (vd: 1, 2)
                    
                    **Response:**
                    - Thông tin chi tiết Space (DTO)
                    
                    **Use cases:**
                    - Admin dashboard
                    - Link nội bộ dựa trên ID
                    - Load thông tin parent space
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy Space"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Space (Sai ID)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi server", content = @Content)
    })
    @GetMapping("/spaces/{spacesId}/by-id")
    public SpaceResponse getSpaces(@PathVariable Long spacesId) {
        return spaceService.getSpaceById(spacesId);
    }

    @Operation(
            summary = "Tham gia Space",
            description = """
                    **Tham gia một Space (cộng đồng)**

                    **Cách hoạt động:**
                    - Nếu chưa là thành viên → Tạo membership mới với role = "member"
                    - Nếu đã rời đi trước đó → Kích hoạt lại membership
                    - Nếu đã là thành viên → Trả về thông tin hiện tại

                    **Response:**
                    - `space_id`: ID của space
                    - `user_id`: ID của user
                    - `is_member`: true nếu đã là thành viên
                    - `role`: Vai trò trong space (member, admin, etc.)
                    - `message`: Thông báo kết quả
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpaceMembershipResponse.class))),
            @ApiResponse(responseCode = "404", description = "Space không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/spaces/{space_id}/join")
    public ResponseEntity<SpaceMembershipResponse> joinSpace(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("space_id") Long spaceId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        SpaceMembershipResponse response = spaceService.joinSpace(spaceId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Rời khỏi Space",
            description = """
                    **Rời khỏi một Space (cộng đồng)**

                    **Cách hoạt động:**
                    - Nếu là thành viên → Đánh dấu status = "left" (soft delete)
                    - Nếu là admin → Không cho phép rời (phải chuyển quyền trước)
                    - Nếu không phải thành viên → Trả về thông báo

                    **Response:**
                    - `space_id`: ID của space
                    - `user_id`: ID của user
                    - `is_member`: false nếu đã rời
                    - `role`: null sau khi rời
                    - `message`: Thông báo kết quả
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpaceMembershipResponse.class))),
            @ApiResponse(responseCode = "404", description = "Space không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Admin không thể rời space")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/spaces/{space_id}/leave")
    public ResponseEntity<SpaceMembershipResponse> leaveSpace(
            @Parameter(hidden = true) Principal principal,
            @PathVariable("space_id") Long spaceId) throws AppException {

        Long userId = getUserIdFromPrincipal(principal);
        SpaceMembershipResponse response = spaceService.leaveSpace(spaceId, userId);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromPrincipal(Principal principal) throws AppException {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        return user.getId();
    }
}
