package com.hth.udecareer.controllers;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.DevicePlatform;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Device Token Management",
     description = "APIs để quản lý FCM device tokens cho push notifications. " +
                   "Mobile apps sử dụng các APIs này để register/deactivate FCM tokens. " +
                   "⚠️ Yêu cầu JWT Authentication - phải có Bearer token trong header.")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;
    private final UserRepository userRepository;


    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
        return user.getId();
    }


    @PostMapping("/register-token")
    @Operation(summary = "Register FCM device token",
               description = "Mobile app gọi API này để register hoặc update FCM token. " +
                           "⚠️ Yêu cầu JWT token để xác thực user.")
    public ResponseEntity<ApiResponse> registerDeviceToken(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam String deviceToken,
            @RequestParam DevicePlatform platform,
            @RequestParam(required = false) String deviceInfo) {

        try {
            if (deviceToken == null || deviceToken.trim().isEmpty()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Device token is required");
            }

            Long userId = getUserIdFromAuthentication(authentication);

            deviceTokenService.registerDeviceToken(userId, deviceToken.trim(), platform, deviceInfo);

            log.info("Registered device token for user {} on platform {} with info: {}",
                    userId, platform, deviceInfo != null ? deviceInfo : "N/A");

            return ResponseEntity.ok(ApiResponse.success("Device token registered successfully"));

        } catch (AppException e) {
            log.error("App exception when registering device token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to register device token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
        }
    }

    @PostMapping("/deactivate-token")
    @Operation(summary = "Deactivate device token",
               description = "Gọi khi user logout khỏi device. ⚠️ Yêu cầu JWT token để xác thực user.")
    public ResponseEntity<ApiResponse> deactivateToken(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam String deviceToken) {

        try {
            if (deviceToken == null || deviceToken.trim().isEmpty()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Device token is required");
            }

            Long userId = getUserIdFromAuthentication(authentication);

            deviceTokenService.deactivateToken(userId, deviceToken.trim());

            log.info("Deactivated device token for user {}", userId);

            return ResponseEntity.ok(ApiResponse.success("Device token deactivated successfully"));

        } catch (AppException e) {
            log.error("App exception when deactivating device token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to deactivate device token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
        }
    }

    @PostMapping("/deactivate-all")
    @Operation(summary = "Deactivate all device tokens",
               description = "Logout khỏi tất cả devices. ⚠️ Yêu cầu JWT token để xác thực user.")
    public ResponseEntity<ApiResponse> deactivateAllTokens(
            @Parameter(hidden = true) Authentication authentication) {

        try {
            Long userId = getUserIdFromAuthentication(authentication);

            deviceTokenService.deactivateAllUserTokens(userId);

            log.info("Deactivated all device tokens for user {}", userId);

            return ResponseEntity.ok(ApiResponse.success("All device tokens deactivated successfully"));

        } catch (AppException e) {
            log.error("App exception when deactivating all device tokens: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to deactivate all device tokens: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
        }
    }

    @GetMapping("/count")
    @Operation(summary = "Get device count",
               description = "Lấy số lượng devices đang active của user. ⚠️ Yêu cầu JWT token để xác thực user.")
    public ResponseEntity<ApiResponse> getDeviceCount(
            @Parameter(hidden = true) Authentication authentication) {

        try {
            Long userId = getUserIdFromAuthentication(authentication);

            long count = deviceTokenService.countUserDevices(userId);

            return ResponseEntity.ok(ApiResponse.success(count));

        } catch (AppException e) {
            log.error("App exception when getting device count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to get device count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
        }
    }
}
