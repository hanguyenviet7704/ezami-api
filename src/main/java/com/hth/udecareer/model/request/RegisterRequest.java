package com.hth.udecareer.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
    description = "Request đăng ký tài khoản mới (Legacy - không cần mã xác thực). " +
                  "Khuyến nghị sử dụng RegisterWithCodeRequest với mã xác thực để bảo mật tốt hơn. " +
                  "Có thể truyền `affiliateId` nếu đăng ký thông qua affiliate link."
)
public class RegisterRequest {
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    @Schema(
        description = "Địa chỉ email của người dùng",
        example = "newuser@example.com",
        required = true
    )
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 7, message = "INVALID_PASSWORD")
    @Schema(
        description = "Mật khẩu cho tài khoản mới (tối thiểu 7 ký tự)",
        example = "MyPassword123",
        required = true,
        minLength = 7
    )
    private String password;

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
