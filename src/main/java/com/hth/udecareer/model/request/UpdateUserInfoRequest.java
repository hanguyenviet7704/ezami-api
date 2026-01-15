package com.hth.udecareer.model.request;

import com.hth.udecareer.enums.Gender;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "Request to update user information (all fields optional)")
public class UpdateUserInfoRequest {

    @Size(max = 100, message = "DISPLAY_NAME_TOO_LONG")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "INVALID_DISPLAY_NAME")
    @Schema(description = "Full name (alias for displayName if displayName is empty)", example = "Nguyen Van A")
    private String fullName;

    @Size(max = 100, message = "FULL_NAME_TOO_LONG")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "INVALID_FULL_NAME")
    @Schema(description = "Display name (preferred over fullName if provided)", example = "Nguyen Van A")
    private String displayName;

    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "INVALID_NICE_NAME")
    @Schema(description = "Nickname (stored in user_nicename)", example = "nguyenvana")
    private String niceName;

    @Schema(description = "Email (cannot be updated, for compatibility only)", example = "user@example.com", deprecated = true)
    private String email;

    @Size(max = 255, message = "INVALID_IMAGE_URL")
    @Schema(description = "Avatar URL (stored in UserMeta with key url_image)", example = "https://cdn.example.com/avatars/u123.png")
    private String urlImage;


    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "INVALID_DOB_FORMAT")
    @Schema(description = "Date of birth (format YYYY-MM-DD)", example = "1990-01-15")
    private String dob;


    @Pattern(regexp = "^(\\+84|0)(3|5|7|8|9)\\d{8}$", message = "INVALID_PHONE_FORMAT")
    @Schema(description = "Phone number", example = "0912345678")
    private String phone;

    @Schema(description = "Country", example = "Vietnam")
    private String country;

    @Schema(description = "Gender (MALE, FEMALE, OTHER)", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private Gender gender;
}
