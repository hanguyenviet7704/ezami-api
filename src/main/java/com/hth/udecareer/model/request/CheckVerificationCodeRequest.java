package com.hth.udecareer.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.hth.udecareer.enums.VerificationCodeType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request kiểm tra tính hợp lệ của mã xác thực")
public class CheckVerificationCodeRequest {
    @NotBlank
    @Email
    @Schema(
        description = "Địa chỉ email đã nhận mã xác thực",
        example = "user@example.com",
        required = true
    )
    private String email;

    @NotBlank
    @Schema(
        description = "Mã xác thực 6 chữ số cần kiểm tra",
        example = "123456",
        required = true,
        minLength = 6,
        maxLength = 6
    )
    private String verificationCode;

    @NotNull
    @Schema(
        description = "Loại mã xác thực",
        example = "REGISTER",
        required = true,
        allowableValues = {"REGISTER", "RESET_PASS"}
    )
    private VerificationCodeType type;
}
