package com.hth.udecareer.model.response;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestionPartInfo extends QuestionTypeInfo {

    @Serial
    private static final long serialVersionUID = -4043128830549640618L;
}
