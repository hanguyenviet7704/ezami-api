package com.hth.udecareer.security;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request đăng nhập - chứa thông tin xác thực người dùng")
public class JwtRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    @Schema(
        description = "Email của người dùng (được sử dụng như username)",
        example = "user@example.com",
        required = true
    )
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Schema(
        description = "Mật khẩu của người dùng",
        example = "Password123!",
        required = true,
        minLength = 6
    )
    private String password;
}
