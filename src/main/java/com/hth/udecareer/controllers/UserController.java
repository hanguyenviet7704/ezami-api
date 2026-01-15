package com.hth.udecareer.controllers;

import java.security.Principal;

import javax.validation.constraints.NotNull;
import javax.validation.Valid;

import com.hth.udecareer.model.request.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.UserResponse;
import com.hth.udecareer.model.response.UserFullResponse;
import com.hth.udecareer.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "User Account Management")
public class UserController {
        private final UserService userService;

    @Operation(
            summary = "Lấy thông tin người dùng hiện tại",
            description = """
                    **Lấy thông tin profile đầy đủ của người dùng đang đăng nhập**
                    
                    API này trả về thông tin cá nhân đầy đủ của người dùng hiện tại dựa vào JWT token trong header Authorization hoặc cookie. 
                    Thông tin bao gồm cả dữ liệu từ bảng User và UserMeta.
                    
                    **Yêu cầu:**
                    - Phải có JWT token hợp lệ trong:
                      - Header: `Authorization: Bearer <token>` (ưu tiên)
                      - Hoặc Cookie: `accessToken=<token>`
                    - Token phải còn hiệu lực (chưa hết hạn)
                    
                    **Response Structure:**
                    
                    **Thông tin cơ bản (từ bảng User - luôn có):**
                    - `id`: ID duy nhất của người dùng trong hệ thống
                    - `username`: Tên đăng nhập (được tạo tự động từ email khi đăng ký)
                      - Format: `{prefix}_{number}` (ví dụ: `nguyen_1`, `tran_2`)
                    - `email`: Địa chỉ email đã đăng ký
                    - `displayName`: Tên hiển thị (có thể cập nhật)
                    - `niceName`: Tên thân thiện/nickname (có thể cập nhật)
                    
                    **Thông tin bổ sung (từ bảng UserMeta - có thể null):**
                    - `url_image`: URL avatar của người dùng
                      - Format: URL string hoặc path (ví dụ: `/uploads/avatars/xxx.jpg`)
                      - Null nếu chưa upload avatar
                    - `phone`: Số điện thoại
                      - Format: Số điện thoại Việt Nam (ví dụ: `0912345678`)
                      - Null nếu chưa cập nhật
                    - `country`: Quốc gia
                      - Format: Tên quốc gia (ví dụ: `Việt Nam`)
                      - Null nếu chưa cập nhật
                    - `dob`: Ngày sinh
                      - Format: `YYYY-MM-DD` (ví dụ: `1990-01-15`)
                      - Null nếu chưa cập nhật
                    - `gender`: Giới tính
                      - Giá trị: `MALE`, `FEMALE`, `OTHER` (từ enum Gender)
                      - Null nếu chưa cập nhật
                    
                    **Ví dụ response đầy đủ:**
                    ```json
                    {
                      "id": 123,
                      "username": "nguyen_1",
                      "email": "user@example.com",
                      "displayName": "Nguyễn Văn A",
                      "niceName": "nguyenvana",
                      "url_image": "https://cdn.example.com/avatars/u123.png",
                      "phone": "0912345678",
                      "country": "Việt Nam",
                      "dob": "1990-01-15",
                      "gender": "MALE"
                    }
                    ```
                    
                    **Ví dụ response khi chưa cập nhật thông tin bổ sung:**
                    ```json
                    {
                      "id": 123,
                      "username": "nguyen_1",
                      "email": "user@example.com",
                      "displayName": "Nguyễn Văn A",
                      "niceName": "nguyen_1",
                      "url_image": null,
                      "phone": null,
                      "country": null,
                      "dob": null,
                      "gender": null
                    }
                    ```
                    
                    **Lưu ý:**
                    - Tất cả các trường từ UserMeta (`url_image`, `phone`, `country`, `dob`, `gender`) có thể là `null` nếu chưa được cập nhật
                    - `id`, `username`, `email` luôn có giá trị (không null)
                    - `displayName` và `niceName` có thể trùng với `username` nếu chưa cập nhật
                    - `gender` trả về dạng enum string (`MALE`, `FEMALE`, `OTHER`), không phải display name
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserFullResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping({"/user/info", "/user/me"})
    public UserResponse getUserInfo(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal)
            throws AppException {
        log.info("getUserInfo: user {}", principal.getName());
        return userService.findByEmail(principal.getName());
    }

    @Operation(
            summary = "Cập nhật thông tin người dùng (tất cả field optional)",
            description = """
                    **Cập nhật thông tin profile của người dùng đang đăng nhập**
                    
                    API này cho phép cập nhật thông tin cá nhân của người dùng. Tất cả các trường trong request đều **optional** - chỉ các trường có giá trị khác null và khác chuỗi rỗng (sau khi trim) mới được cập nhật.
                    
                    **Thông tin cơ bản (lưu vào bảng User):**
                    - `displayName`: Tên hiển thị (ưu tiên cao nhất)
                      - Tối đa 100 ký tự
                      - Chỉ cho phép chữ cái và khoảng trắng (a-zA-Z và Unicode letters)
                      - Lỗi: `FULL_NAME_TOO_LONG` hoặc `INVALID_FULL_NAME`
                    - `fullName`: Alias của displayName; chỉ dùng khi `displayName` không được gửi
                      - Tối đa 100 ký tự
                      - Chỉ cho phép chữ cái và khoảng trắng
                      - Lỗi: `DISPLAY_NAME_TOO_LONG` hoặc `INVALID_DISPLAY_NAME`
                    - `niceName`: Biệt danh (lưu vào `user_nicename`)
                      - Độ dài: 3-50 ký tự
                      - Chỉ cho phép: chữ cái (a-zA-Z), dấu chấm (.), dấu gạch dưới (_), dấu gạch ngang (-)
                      - Lỗi: `INVALID_NICE_NAME`
                    
                    **Thông tin bổ sung (lưu vào bảng UserMeta):**
                    - `urlImage`: URL avatar (lưu vào UserMeta với key `url_image`)
                      - Tối đa 255 ký tự
                      - Lỗi: `INVALID_IMAGE_URL`
                    - `phone`: Số điện thoại Việt Nam
                      - Format: `(+84|0)(3|5|7|8|9)[0-9]{8}`
                      - Ví dụ hợp lệ: `0912345678`, `+84912345678`, `0987654321`
                      - Lỗi: `INVALID_PHONE_FORMAT`
                    - `country`: Quốc gia
                      - Chỉ cập nhật nếu tồn tại trong cache
                      - Không báo lỗi nếu giá trị không hợp lệ (chỉ bỏ qua)
                    - `dob`: Ngày sinh
                      - Format: `YYYY-MM-DD` (ví dụ: `1990-01-15`)
                      - Lỗi: `INVALID_DOB_FORMAT`
                    - `gender`: Giới tính
                      - Enum: `MALE` (Nam), `FEMALE` (Nữ), `OTHER` (Khác)
                      - Nếu null → bỏ qua, không cập nhật
                    
                    **Trường bị bỏ qua (không cập nhật DB):**
                    - `email`: Giữ để tương thích payload cũ, không được cập nhật
                    
                    **Ví dụ payload đơn giản:**
                    ```json
                    {
                      "fullName": "Nguyễn Văn A"
                    }
                    ```
                    
                    **Ví dụ payload đầy đủ:**
                    ```json
                    {
                      "displayName": "Nguyễn Văn A",
                      "niceName": "nguyenvana",
                      "urlImage": "https://cdn.example.com/avatars/u123.png",
                      "phone": "0912345678",
                      "country": "Việt Nam",
                      "dob": "1990-01-15",
                      "gender": "MALE"
                    }
                    ```
                    
                    **Lưu ý:**
                    - Có thể cập nhật một hoặc nhiều trường cùng lúc
                    - Các trường không gửi trong request sẽ giữ nguyên giá trị cũ
                    - Giá trị rỗng hoặc chỉ chứa khoảng trắng sẽ bị bỏ qua
                    - `displayName` có ưu tiên cao hơn `fullName` nếu cả hai đều được gửi
                    - `gender` phải là một trong: `MALE`, `FEMALE`, `OTHER` (chữ hoa)
                    """
    )

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/user/update")
    public void update(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin người dùng cần cập nhật (tất cả field đều optional)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateUserInfoRequest.class))
            ) @Valid @RequestBody UpdateUserInfoRequest request)
            throws AppException {
        log.info("userUpdate: user {}", principal.getName());
        userService.updateUserInfo(request, principal.getName());
    }

    @Operation(summary = "Xóa tài khoản người dùng", description = """
            **Xóa vĩnh viễn tài khoản của người dùng hiện tại**
            
            API này sẽ:
            - Xóa hoàn toàn tài khoản người dùng khỏi hệ thống
            - Xóa tất cả dữ liệu liên quan (quiz history, progress, etc.)
            - Token hiện tại sẽ không còn hợp lệ sau khi xóa
            
            **Yêu cầu:**
            - Phải có JWT token hợp lệ
            
            **Khuyến nghị:**
            - Nên có dialog xác nhận trước khi gọi API này
            - Thông báo rõ ràng cho người dùng về hậu quả
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa tài khoản thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/user/delete")
    public void delete(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @RequestBody(required = false) DeleteAccountRequest request)
            throws AppException {
        log.info("userDelete: user {}", principal.getName());
        userService.deleteAccount(principal.getName(), request);
    }

    @Operation(summary = "Generate verification code (Authenticated)", description = "Sends a verification code to the authenticated user's email (e.g., for changing email).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid type", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content),
            @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/user/verification-code")
    public void generateVerificationCode(@NotNull Principal principal,
                                         @Valid @NotNull @RequestBody VerificationRequest request) throws Exception {
        log.info("generateVerificationCode: user {}, type {}", principal.getName(), request.getType());
        userService.generateVerificationCode(principal.getName(), request.getType());
    }

    @Operation(summary = "Đổi mật khẩu", description = """
            **Đổi mật khẩu cho người dùng đã đăng nhập**
            
            API này cho phép người dùng thay đổi mật khẩu bằng cách cung cấp:
            - Mật khẩu cũ (để xác thực)
            - Mật khẩu mới (tối thiểu 8 ký tự)
            
            **Yêu cầu:**
            - Phải có JWT token hợp lệ
            - Mật khẩu cũ phải chính xác
            - Mật khẩu mới phải tối thiểu 8 ký tự
            
            **Flow:**
            1. Hệ thống xác minh mật khẩu cũ
            2. Nếu đúng, cập nhật mật khẩu mới
            3. Token hiện tại vẫn còn hiệu lực (không cần đăng nhập lại)
            
            **Lỗi phổ biến:**
            - OLD_PASSWORD_INCORRECT: Mật khẩu cũ không đúng
            - INVALID_PASSWORD: Mật khẩu mới không đủ yêu cầu
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ sai hoặc mật khẩu mới không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/user/change-pass")
    public void changePass(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Mật khẩu cũ và mật khẩu mới", required = true) @Valid @RequestBody ChangePassRequest request)
            throws AppException {
        log.info("changePass: user {}", principal.getName());
        userService.changePass(request, principal.getName());
    }

    @Operation(summary = "Reset password (Forgot)", description = "Public API to reset a user's password using an email and a verification code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid email, code, or password", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })

    @PostMapping("/user/reset-pass")
    public void resetPass(@Valid @RequestBody ResetPasswordRequest request) throws AppException {
        log.info("resetPass: user {}", request.getEmail());
        userService.resetPass(request);
    }

    @Operation(
            summary = "Complete onboarding",
            description = """
                    **Mark user onboarding as completed**

                    This API saves the onboarding completion status to user metadata.

                    **Request:**
                    - `selectedCareerPath`: Optional career path selected (e.g., "SCRUM_MASTER", "DEVELOPER")
                    - `selectedCertifications`: Optional list of certification codes
                    - `targetDate`: Optional target completion date (YYYY-MM-DD)

                    **What it does:**
                    - Saves `onboarding_completed=true` to wp_usermeta
                    - Saves selected career path and certifications if provided
                    - Returns updated user info

                    **Yêu cầu:**
                    - Phải có JWT token hợp lệ
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Onboarding completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "default", description = "Unexpected error")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/user/onboarding/complete")
    public UserResponse completeOnboarding(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @Valid @RequestBody(required = false) OnboardingCompleteRequest request)
            throws AppException {
        log.info("completeOnboarding: user {}", principal.getName());
        return userService.completeOnboarding(principal.getName(), request);
    }

    @Operation(
            summary = "Get mock test readiness",
            description = """
                    **Get user's readiness score for mock tests**

                    Returns the latest readiness snapshot showing:
                    - Overall readiness percentage
                    - Pass probability
                    - Skill breakdown
                    - Recommended actions

                    **Optional query param:**
                    - `testType`: Filter by specific test type (e.g., "PSM_I", "PSPO_I")

                    **Yêu cầu:**
                    - Phải có JWT token hợp lệ
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Readiness data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "No readiness data found", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user/mock-test/readiness")
    public com.hth.udecareer.eil.entities.EilReadinessSnapshotEntity getMockTestReadiness(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.Parameter(description = "Test type filter (optional)", example = "PSM_I")
            @org.springframework.web.bind.annotation.RequestParam(required = false) String testType)
            throws AppException {
        log.info("getMockTestReadiness: user {}, testType {}", principal.getName(), testType);
        return userService.getMockTestReadiness(principal, testType);
    }

}