package com.hth.udecareer.annotation.validation;

import com.hth.udecareer.validation.CategoryCodeValidator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryCodeValidator.class)
@Documented
public @interface ValidCategoryCode {
    String message() default "Invalid category code. Does not exist.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}