package com.hth.udecareer.model.response;

import com.hth.udecareer.entities.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Thông tin người dùng")
public class UserResponse {
    @Schema(
            description = "ID duy nhất của người dùng.",
            example = "12"
    )
    private Long id;

    @Schema(
        description = "Tên đăng nhập duy nhất của người dùng (được tạo tự động từ email)",
        example = "nguyen_1"
    )
    private String username;

    @Schema(
        description = "Địa chỉ email của người dùng",
        example = "user@example.com"
    )
    private String email;

    @Schema(
        description = "Tên thân thiện của người dùng",
        example = "nguyen_1"
    )
    private String niceName;


    @Schema(
        description = "Tên hiển thị của người dùng",
        example = "Nguyễn Văn A"
    )
    private String displayName;

    public UserResponse() {
    }

    public static UserResponse from(User user) {
        return builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .niceName(user.getNiceName())
                .displayName(user.getDisplayName())
                .build();
    }
}
