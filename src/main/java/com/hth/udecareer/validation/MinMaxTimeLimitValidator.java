package com.hth.udecareer.validation;

import com.hth.udecareer.annotation.validation.MinMaxTimeLimit;
import com.hth.udecareer.model.dto.QuizSearchRequestDto;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MinMaxTimeLimitValidator implements ConstraintValidator<MinMaxTimeLimit, QuizSearchRequestDto> {

    @Override
    public boolean isValid(QuizSearchRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getMinTimeLimit() == null || dto.getMaxTimeLimit() == null) {
            return true;
        }

        return dto.getMinTimeLimit() <= dto.getMaxTimeLimit();
    }
}