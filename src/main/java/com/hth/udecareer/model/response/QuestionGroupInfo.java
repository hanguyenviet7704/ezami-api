package com.hth.udecareer.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestionGroupInfo extends QuestionTypeInfo {

    @Serial
    private static final long serialVersionUID = 228279009013473299L;

    private int numberOfQuestions;

    private List<QuestionResponse> questions;
}
