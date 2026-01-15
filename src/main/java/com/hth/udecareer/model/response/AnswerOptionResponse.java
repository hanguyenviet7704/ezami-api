package com.hth.udecareer.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerOptionResponse {
    private Integer index;
    private String text;
}
