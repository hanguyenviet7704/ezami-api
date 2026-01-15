package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.SupportLogRequest;
import com.hth.udecareer.model.request.SupportProcessRequest;
import com.hth.udecareer.model.request.SupportRequestDto;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.SupportLogResponse;
import com.hth.udecareer.model.response.SupportResponse;
import com.hth.udecareer.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Support Management")
public class SupportController {

    private final SupportService supportService;

    @Operation(
            summary = "Lấy danh sách kênh hỗ trợ",
            description = """
                    **Lấy danh sách các kênh hỗ trợ hiện có**
                    
                    Trả về danh sách các kênh hỗ trợ (ví dụ: Email, Zalo, Telegram, Hotline...)
                    dưới dạng key/value để client hiển thị cho người dùng.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách kênh hỗ trợ thành công"
            )
    })
    @GetMapping("/support/channels")
    public Map<String, String> getSupportChannels() {
        return supportService.getSupportChannels();
    }

    @Operation(
            summary = "Lưu log tương tác hỗ trợ",
            description = """
                    **Ghi nhận log cho các hoạt động hỗ trợ**
                    
                    API này dùng để lưu lại các hành động click hỗ trợ người dùng và 1 kênh hỗ trợ nào đó
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lưu log hỗ trợ thành công"
            )
    })
    @PostMapping("/support/log")
    public SupportLogResponse saveLog(@RequestBody SupportLogRequest request,
                                      Principal principal) {
        return supportService.saveLog(request, principal);
    }

    @Operation(
            summary = "Gửi yêu cầu hỗ trợ (Create Ticket)",
            description = """
                    **Tạo yêu cầu hỗ trợ hoặc báo lỗi từ App**
                    
                    API này cho phép người dùng gửi thông tin lỗi, góp ý kèm theo ảnh minh họa và thông tin thiết bị.
                    Hệ thống sẽ ghi nhận ticket với trạng thái mặc định là `PENDING` (Chờ xử lý).
                    
                    **Flow:**
                    1. Client (Mobile/Web) upload ảnh qua API Upload Image để lấy danh sách URL.
                    2. Client tự thu thập thông tin thiết bị (OS, Model, AppVersion, OsVersion).
                    3. Client gọi API này với đầy đủ thông tin để tạo ticket.
                    
                    **Yêu cầu:**
                    - **Auth:** Bearer Token (User đã đăng nhập).
                    - **Input:** `description` là bắt buộc.
                    - **Limit:** Tối đa 5 ảnh minh họa.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Gửi yêu cầu thành công",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi dữ liệu (Thiếu mô tả, sai định dạng ảnh, quá số lượng...)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc Token hết hạn",
                    content = @Content
            )
    })

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/support/request")
    public ResponseEntity<Map<String, Object>> getSupportRequest(Principal principal,
                                              @Valid @RequestBody SupportRequestDto request) {

        supportService.createSupportTicket(principal.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Support request sent successfully!"));
    }

    @Operation(
            summary = "Xem lịch sử hỗ trợ (History)",
            description = """
                    **Lấy danh sách các yêu cầu hỗ trợ.**
                    
                    - **Admin:** Xem toàn bộ hệ thống (kèm User Info, Device Info).
                    - **User:** Chỉ xem của chính mình.
                    
                    **Trả về:** Object PageResponse chứa danh sách vé.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/support/history")
    public  PageResponse<SupportResponse> getSupportHistory(Principal principal,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        return supportService.getSupportHistory(principal.getName(), page, size);
    }

    @GetMapping("/support/{supportId}")
    public SupportResponse getSupportDetail(@PathVariable Long supportId) {
        return supportService.getSupportDetail(supportId);
    }

    @PostMapping("/support/process")
    public SupportResponse processSupport(@RequestBody SupportProcessRequest request) {
        return supportService.processSupport(request);
    }
}
