package com.hth.udecareer.model.response;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hth.udecareer.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // tra ve nhung field k bi null
@Schema(description = "Cấu trúc response chung cho các API error responses")
public class ApiResponse {
    @Schema(description = "HTTP status code", example = "400")
    private int code;

    @Schema(description = "Thông báo lỗi hoặc thành công", example = "Invalid request data")
    private String message;

    @Schema(description = "Dữ liệu trả về (nếu có)", example = "{}")
    private Object data;

    @Schema(description = "Thông tin lỗi chi tiết (nếu có)", example = "Validation failed")
    private Object error;

    public static ApiResponse success() {
        return builder()
                .code(HttpStatus.OK.value())
                .build();
    }

    public static ApiResponse success(Object data) {
        return builder()
                .code(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    public static ApiResponse fail(@NotNull HttpStatus status,
            String message) {
        return builder()
                .code(status.value())
                .message(message)
                .build();
    }

    public static ApiResponse fail(Integer status,
            String message) {
        return builder()
                .code(status)
                .message(message)
                .build();
    }

    public static ApiResponse fail(ErrorCode errorCode) {
        return builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ApiResponse fail(ErrorCode errorCode, String customMessage) {
        return builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }
}
