package com.hth.udecareer.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response chứa JWT token sau khi đăng nhập/đăng ký thành công")
public class JwtResponse {

    @Schema(
        description = "JWT access token để xác thực các API calls tiếp theo. Token này cần được gửi trong header Authorization của các request.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String jwtToken;
}
