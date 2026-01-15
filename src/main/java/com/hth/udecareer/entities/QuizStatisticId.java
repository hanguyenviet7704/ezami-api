package com.hth.udecareer.entities;

import java.io.Serial;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class QuizStatisticId implements Serializable {

    @Serial
    private static final long serialVersionUID = -6981896662015874099L;

    @Column(name = "statistic_ref_id")
    private Long statisticRefId;

    @Column(name = "question_id")
    private Long questionId;
}
