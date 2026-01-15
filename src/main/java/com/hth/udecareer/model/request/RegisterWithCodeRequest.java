package com.hth.udecareer.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
    description = "Request đăng ký tài khoản mới với mã xác thực email. " +
                  "Người dùng cần gọi API `/verification-code` trước để nhận mã xác thực, sau đó dùng mã đó để đăng ký. " +
                  "Có thể truyền `affiliateId` nếu đăng ký thông qua affiliate link."
)
public class RegisterWithCodeRequest {
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    @Schema(
        description = "Địa chỉ email của người dùng (phải là email hợp lệ)",
        example = "newuser@example.com",
        required = true
    )
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Schema(
        description = "Mật khẩu cho tài khoản mới (tối thiểu 6 ký tự)",
        example = "SecurePassword123!",
        required = true,
        minLength = 6
    )
    private String password;

    @NotBlank(message = "INVALID_CODE")
    @Schema(
        description = "Mã xác thực 6 chữ số được gửi qua email",
        example = "123456",
        required = true,
        minLength = 6,
        maxLength = 6
    )
    private String verificationCode;

    @NotBlank(message = "FULLNAME_REQUIRED")
    @Size(max = 100, message = "FULLNAME_TOO_LONG")
    @Schema(
        description = "Họ và tên đầy đủ của người dùng",
        example = "Nguyễn Văn A",
        required = true,
        maxLength = 100
    )
    private String fullName;

    @Schema(
        description = "Mã ứng dụng (app code). Mặc định là 'ezami' nếu không truyền",
        example = "ezami",
        required = false
    )
    private String appCode;

    @Schema(
        description = "ID của affiliate nếu người dùng đăng ký thông qua affiliate link. Dùng để track referral và tính commission",
        example = "123",
        required = false
    )
    private Long affiliateId;
}
