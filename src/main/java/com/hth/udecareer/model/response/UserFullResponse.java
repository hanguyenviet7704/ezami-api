package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Thông tin người dùng kèm meta (gộp chung)")
public class UserFullResponse extends UserResponse {


    @Schema(description = "Url ảnh của người dùng",
            example = "https://cdn.example.com/avatars/u123.png")
    private String url_image;

    @Schema(description = "Số điện thoại của người dùng",
            example = "0987654321")
    private String phone;

    @Schema(description = "Quốc gia của người dùng",
            example = "Việt Nam")
    private String country;

    @Schema(description = "Ngày sinh của người dùng (định dạng YYYY-MM-DD)",
            example = "1990-01-15")
    private String dob;

    @Schema(description = "Giới tính của người dùng (MALE, FEMALE, OTHER)",
            example = "MALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

}
