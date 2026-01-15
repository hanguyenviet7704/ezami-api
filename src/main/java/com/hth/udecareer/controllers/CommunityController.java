package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.response.CommunitySyncResponse;
import com.hth.udecareer.service.CommunitySyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Community", description = "APIs for community features integration")
public class CommunityController {

    private final CommunitySyncService communitySyncService;

    @Operation(
            summary = "Sync session với WordPress Community",
            description = """
                    **Đồng bộ session giữa Java backend và WordPress community**

                    API này được gọi từ mobile app trước khi mở WebView community để:
                    1. Xác thực user qua JWT token
                    2. Lấy thông tin user để sync với WordPress
                    3. Trả về session info

                    **Yêu cầu:**
                    - JWT token hợp lệ trong header Authorization

                    **Response:**
                    - `success`: true nếu sync thành công
                    - `message`: Thông báo kết quả
                    - `wordpressUserId`: ID của user trong WordPress
                    - `displayName`: Tên hiển thị của user

                    **Lưu ý:**
                    - Endpoint này non-blocking, app vẫn hoạt động bình thường nếu sync fail
                    - Session cookie (nếu có) sẽ được trả qua Set-Cookie header
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sync session thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommunitySyncResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực - Token không hợp lệ hoặc thiếu",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy user",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/community/sync-session")
    public ResponseEntity<CommunitySyncResponse> syncSession(Principal principal) {
        log.info("Community sync-session request from user: {}", principal.getName());

        CommunitySyncResponse response = communitySyncService.syncSession(principal.getName());

        return ResponseEntity.ok(response);
    }
}
