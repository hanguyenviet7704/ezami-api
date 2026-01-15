package com.hth.udecareer.controllers;

import com.hth.udecareer.entities.NotificationEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.NotificationType;
import com.hth.udecareer.enums.SourceObjectType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.model.dto.NotificationWithStatusDTO;
import com.hth.udecareer.model.request.UpdateNotificationSettingRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.NotificationRepository;
import com.hth.udecareer.service.NotificationService;
import com.hth.udecareer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final com.hth.udecareer.repository.UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;


    @GetMapping("/my")
    @Operation(summary = "Lấy danh sách thông báo của tôi", description = "Lấy danh sách thông báo của người dùng hiện tại với phân trang và tùy chọn chỉ lấy thông báo chưa đọc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<PageResponse<NotificationWithStatusDTO>> getMyNotifications(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng thông báo trên mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Chỉ lấy thông báo chưa đọc", example = "false")
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        Long userId = user.getId();
        log.info("Getting notifications for user {} (email: {}), unreadOnly: {}", userId, email, unreadOnly);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<NotificationWithStatusDTO> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findUnreadNotificationDTOsForUser(userId, pageable);
        } else {
            notifications = notificationRepository.findNotificationDTOsForUser(userId, pageable);
        }

        return ResponseEntity.ok(PageResponse.of(notifications));
    }


    @PutMapping("/{id}/mark-read")
    @Operation(summary = "Đánh dấu thông báo đã đọc", description = "Đánh dấu một thông báo cụ thể là đã đọc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "ID của thông báo cần đánh dấu đã đọc", example = "1")
            @PathVariable Long id
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        Long userId = user.getId();

        // Use new service method that works with wp_fcom_notification_users table
        boolean success = notificationService.markNotificationAsRead(userId, id);

        if (!success) {
            throw new RuntimeException("Notification not found or already processed");
        }

        log.info("Notification {} marked as read by user {}", id, userId);

        return ResponseEntity.ok().build();
    }


    @PutMapping("/mark-all-read")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc", description = "Đánh dấu tất cả thông báo chưa đọc của người dùng hiện tại là đã đọc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) Principal principal
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        Long userId = user.getId();

        // Use new service method that works with wp_fcom_notification_users table
        int updatedCount = notificationService.markAllNotificationsAsRead(userId);

        log.info("Marked {} notifications as read for user {}", updatedCount, userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thông báo", description = "Xóa 1 thông báo theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
    public ResponseEntity<Void> deleteNotification(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "ID của thông báo cần xóa", example = "1")
            @PathVariable Long id
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        Long userId = user.getId();

        boolean success = notificationService.deleteUserNotification(userId, id);

        if (!success) {
            throw new RuntimeException("Notification not found");
        }

        log.info("Notification {} deleted by user {}", id, userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Xóa tất cả thông báo", description = "Xóa toàn bộ thông báo của người dùng hiện tại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập")
    })
    public ResponseEntity<Void> deleteAllNotifications(
            @Parameter(hidden = true) Principal principal
    ) {
        if (principal == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        Long userId = user.getId();

        notificationService.deleteAllUserNotifications(userId);

        log.info("Deleted all notifications for user {}", userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-send")
    @Operation(summary = "Gửi thông báo test", description = "API để test gửi thông báo với nội dung tùy chọn (không cần JWT)", hidden = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<com.hth.udecareer.model.response.ApiResponse> sendTestNotification(
            @Parameter(description = "ID của người dùng nhận thông báo", example = "1", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Tiêu đề thông báo", example = "Thông báo test", required = true)
            @RequestParam String title,
            @Parameter(description = "Nội dung thông báo", example = "Đây là nội dung thông báo test", required = true)
            @RequestParam String message,
            @Parameter(description = "Loại thông báo", example = "SYSTEM_INFO")
            @RequestParam(defaultValue = "SYSTEM_INFO") String type,
            @Parameter(description = "URL hành động (tùy chọn)", example = "/quiz/123")
            @RequestParam(required = false) String actionUrl,
            @Parameter(description = "ID đối tượng liên quan (đơn hàng, quiz, course, etc.)", example = "123")
            @RequestParam(required = false) Long objectId,
            @Parameter(description = "ID người dùng thực hiện hành động", example = "2")
            @RequestParam(required = false) Long srcUserId,
            @Parameter(description = "Loại đối tượng (order, quiz, course, comment, feed)", example = "order")
            @RequestParam(required = false) String srcObjectType
    ) {
        try {
            // Validate input
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(
                    com.hth.udecareer.model.response.ApiResponse.builder()
                            .code(400)
                            .message("User ID không hợp lệ")
                            .build()
                );
            }

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    com.hth.udecareer.model.response.ApiResponse.builder()
                            .code(400)
                            .message("Tiêu đề không được để trống")
                            .build()
                );
            }

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    com.hth.udecareer.model.response.ApiResponse.builder()
                            .code(400)
                            .message("Nội dung không được để trống")
                            .build()
                );
            }

            // Parse notification type
            NotificationType notificationType;
            try {
                notificationType = NotificationType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                notificationType = NotificationType.SYSTEM_INFO;
                log.warn("Invalid notification type '{}', using SYSTEM_INFO as default", type);
            }

            // Parse source object type
            SourceObjectType sourceObjectType = null;
            if (srcObjectType != null && !srcObjectType.trim().isEmpty()) {
                sourceObjectType = SourceObjectType.fromString(srcObjectType.trim());
                if (sourceObjectType == null) {
                    log.warn("Invalid source object type '{}', setting to null", srcObjectType);
                }
            }

            // Create and send notification with new fields
            notificationService.createNotification(
                    userId,
                    title.trim(),
                    message.trim(),
                    notificationType,
                    actionUrl != null ? actionUrl.trim() : null,
                    objectId,
                    srcUserId,
                    sourceObjectType
            );

            log.info("Test notification sent successfully to user {} with title '{}' [objectId={}, srcUserId={}, srcObjectType={}]",
                    userId, title, objectId, srcUserId, sourceObjectType);

            return ResponseEntity.ok(
                com.hth.udecareer.model.response.ApiResponse.builder()
                        .code(200)
                        .message("✅ Thông báo đã được gửi thành công!")
                        .build()
            );

        } catch (Exception e) {
            log.error("Failed to send test notification to user {}: {}", userId, e.getMessage(), e);

            return ResponseEntity.status(500).body(
                com.hth.udecareer.model.response.ApiResponse.builder()
                        .code(500)
                        .message("❌ Lỗi gửi thông báo: " + e.getMessage())
                        .build()
            );
        }
    }


    @Operation(
            summary = "Lấy trạng thái cài đặt thông báo (Bật/Tắt)",
            description = "API này trả về trạng thái `allow_push` hiện tại của user. " +
                    "Nếu user chưa từng cài đặt, hệ thống mặc định trả về `true` (Bật).",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lấy dữ liệu thành công",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Response Mẫu",
                                            value = "{\"allow_push\": true}",
                                            description = "Trả về true nếu đang Bật, false nếu đang Tắt"
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc Token hết hạn", content = @Content)
            }
    )

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/setting")
    public ResponseEntity<Map<String, Boolean>> getNotificationSetting(Principal principal) {
        boolean isAllowed = userService.getNotificationSetting(principal.getName());

        return ResponseEntity.ok(Collections.singletonMap("allow_push", isAllowed));
    }

    @Operation(
            summary = "Cập nhật trạng thái thông báo (Bật/Tắt)",
            description = "Cho phép user thay đổi cài đặt nhận thông báo đẩy. " +
                    "Thay đổi có hiệu lực ngay lập tức với các thông báo Push tiếp theo.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cập nhật thành công",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            value = "{\"success\": true}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Lỗi dữ liệu đầu vào (Thiếu field hoặc Sai kiểu dữ liệu)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Error Example",
                                            value = "{\n  \"code\": 1060,\n  \"message\": \"allow_push is required\",\n  \"status\": \"BAD_REQUEST\"\n}"
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Chưa đăng nhập (Unauthorized)", content = @Content)
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/setting")
    public ResponseEntity<Map<String, String>> updateNotificationSetting(@Valid @RequestBody UpdateNotificationSettingRequest request,  Principal principal) {
        userService.updateNotificationSetting(principal.getName(), request.getAllowPush());

        return ResponseEntity.ok(Collections.singletonMap("success", "Cập nhật thành công!"));
    }


}
