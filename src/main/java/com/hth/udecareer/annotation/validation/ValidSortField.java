package com.hth.udecareer.annotation.validation;

import com.hth.udecareer.validation.SortFieldValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;


@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SortFieldValidator.class)
@Documented
public @interface ValidSortField {
    String message() default "Invalid sort field. Allowed fields: id, name, timeLimit, slug, postId, postTitle. Format: field,direction";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

