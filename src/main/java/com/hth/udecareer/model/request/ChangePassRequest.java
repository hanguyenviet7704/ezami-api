package com.hth.udecareer.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request đổi mật khẩu cho người dùng đã đăng nhập")
public class ChangePassRequest {
    @NotBlank(message = "OLD_PASSWORD_REQUIRED")
    @Schema(
        description = "Mật khẩu hiện tại của người dùng",
        example = "OldPassword123",
        required = true
    )
    private String oldPass;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "INVALID_PASSWORD")
    @Schema(
        description = "Mật khẩu mới (tối thiểu 8 ký tự)",
        example = "NewPassword123!",
        required = true,
        minLength = 8
    )
    private String newPass;
}
