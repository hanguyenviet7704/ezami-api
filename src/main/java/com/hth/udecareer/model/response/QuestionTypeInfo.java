package com.hth.udecareer.model.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@RequiredArgsConstructor
public class QuestionTypeInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = -1777054946639831426L;

    private String group;

    private Integer part;

    private QuestionType type;
}
