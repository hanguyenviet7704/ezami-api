package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UpdateLinkRequest {

    @Size(max = 255, message = "Campaign name too long")
    private String campaign;

    @Size(max = 100, message = "Medium name too long")
    private String medium;

    @Size(max = 255, message = "Source name too long")
    private String source;

    private String notes;
}

