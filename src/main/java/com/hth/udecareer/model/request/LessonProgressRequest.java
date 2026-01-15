package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LessonProgressRequest {
    @NotNull(message = "INVALID_LAST_SECOND")
    @Min(value = 0, message = "INVALID_LAST_SECOND")
    @Max(value = 86400, message = "INVALID_LAST_SECOND")
    private Long lastSecond;

    @NotNull(message = "INVALID_DURATION")
    @Min(value = 1, message = "INVALID_DURATION")
    @Max(value = 86400, message = "INVALID_DURATION")
    private Long duration;
}
