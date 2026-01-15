package com.hth.udecareer.model.request;

import com.hth.udecareer.model.dto.DeviceInfo;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class SupportRequestDto {
    @Size(max = 255, message = "SUPPORT_TITLE_TOO_LONG")
    private String title;

    @NotBlank(message = "SUPPORT_DESCRIPTION_REQUIRED")
    @Size(max = 200, message = "SUPPORT_DESCRIPTION_LENGTH")
    private String description;

    @Size(max = 5, message = "SUPPORT_IMAGES_LIMIT_EXCEEDED")
    private List<String> imageUrls;
    private DeviceInfo deviceInfo;
}
