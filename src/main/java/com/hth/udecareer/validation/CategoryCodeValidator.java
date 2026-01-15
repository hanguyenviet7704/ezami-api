package com.hth.udecareer.validation;

import com.hth.udecareer.repository.QuizCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.hth.udecareer.annotation.validation.ValidCategoryCode;

public class CategoryCodeValidator implements ConstraintValidator<ValidCategoryCode, String> {

    @Autowired
    private QuizCategoryRepository quizCategoryRepository;

    @Override
    public boolean isValid(String inputValue, ConstraintValidatorContext context) {
        if (ObjectUtils.isEmpty(inputValue)) {
            return true;
        }

        return quizCategoryRepository.existsByTitle(inputValue);
    }
}