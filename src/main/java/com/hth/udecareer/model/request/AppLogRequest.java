package com.hth.udecareer.model.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class AppLogRequest {
    private String appCode;

    private String deviceOs;
}
