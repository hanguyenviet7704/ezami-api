package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.ApplyReferralRequest;
import com.hth.udecareer.model.response.ReferralCodeResponse;
import com.hth.udecareer.service.ReferralService;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Referral", description = "APIs mời bạn bè và mã giới thiệu")
public class ReferralController {

    private final ReferralService referralService;

    @Operation(
            summary = "Tạo hoặc Lấy mã giới thiệu (Trả về mã code)",
            description = """
                    **Tạo hoặc lấy mã giới thiệu của user hiện tại**
                    
                    - Nếu user chưa có mã, hệ thống tự động tạo mã giới thiệu mới (7 ký tự)
                    - Nếu user đã có mã, trả về mã cũ
                    - Mã giới thiệu là duy nhất cho mỗi user
                    
                    **Response:**
                    - `referralCode`: Mã giới thiệu của user
                    - `isReferral`: true nếu user này được giới thiệu bởi người khác, false nếu không
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"referralCode\": \"Q8W4E3R\", \"isReferral\": false}"))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/referral/create-code")
    public ResponseEntity<ReferralCodeResponse> createReferralCode(Principal principal) {
        log.info("createReferralCode: user {}", principal.getName());
        return ResponseEntity.ok(referralService.createReferralCode(principal.getName()));
    }

    @Operation(
            summary = "Áp dụng mã giới thiệu",
            description = """
                    **Áp dụng mã giới thiệu của người khác để nhận điểm**
                    
                    Khi áp dụng mã giới thiệu thành công:
                    - **Người giới thiệu** nhận **50 điểm** (`POINT_REFERRAL_GIVE`)
                    - **Người được giới thiệu** nhận **10 điểm** (`POINT_REFERRAL_RECEIVE`)
                    
                    **Lưu ý:**
                    - Mỗi user chỉ có thể áp dụng 1 mã giới thiệu duy nhất
                    - Không thể tự giới thiệu chính mình
                    - Người giới thiệu phải đăng ký trước người được giới thiệu
                    - Nếu đã áp dụng mã rồi, sẽ báo lỗi `REFERRAL_ALREADY_APPLIED`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mã giới thiệu đã được áp dụng thành công"),
            @ApiResponse(responseCode = "400", description = "Mã không hợp lệ, tự giới thiệu, hoặc đã áp dụng rồi"),
            @ApiResponse(responseCode = "404", description = "Mã giới thiệu không tồn tại")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/referral/apply-code")
    public ResponseEntity<Map<String, String>> applyReferralCode(
            @Valid @RequestBody ApplyReferralRequest request,
            Principal principal) {
        log.info("applyReferralCode: user {}, code {}", principal.getName(), request.getReferralCode());
        referralService.applyReferralCode(request, principal.getName());
        return ResponseEntity.ok(Collections.singletonMap("message", "Mã giới thiệu đã được áp dụng thành công."));
    }
}

