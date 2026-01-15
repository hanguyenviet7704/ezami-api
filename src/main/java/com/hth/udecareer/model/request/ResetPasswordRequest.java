package com.hth.udecareer.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request đặt lại mật khẩu (Quên mật khẩu) với mã xác thực")
public class ResetPasswordRequest {
    @Email
    @NotBlank
    @Schema(
        description = "Địa chỉ email của tài khoản cần reset mật khẩu",
        example = "user@example.com",
        required = true
    )
    private String email;

    @NotBlank
    @Schema(
        description = "Mã xác thực 6 chữ số được gửi qua email",
        example = "123456",
        required = true,
        minLength = 6,
        maxLength = 6
    )
    private String verificationCode;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "INVALID_PASSWORD")
    @Schema(
        description = "Mật khẩu mới (tối thiểu 8 ký tự)",
        example = "NewSecurePassword123!",
        required = true,
        minLength = 8
    )
    private String password;
}