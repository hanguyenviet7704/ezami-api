package com.hth.udecareer.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.hth.udecareer.enums.VerificationCodeType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request tạo mã xác thực gửi qua email")
public class VerificationRequest {
    @NotNull(message = "INVALID_TYPE")
    @Schema(
        description = "Loại mã xác thực cần tạo",
        example = "REGISTER",
        required = true,
        allowableValues = {"REGISTER", "RESET_PASS"}
    )
    private VerificationCodeType type;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    @Schema(
        description = "Địa chỉ email nhận mã xác thực",
        example = "user@example.com",
        required = true
    )
    private String email;
}
