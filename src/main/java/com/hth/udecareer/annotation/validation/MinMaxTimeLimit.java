package com.hth.udecareer.annotation.validation;

import com.hth.udecareer.validation.MinMaxTimeLimitValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinMaxTimeLimitValidator.class)
public @interface MinMaxTimeLimit {
    String message() default "minTimeLimit must be less than or equal to maxTimeLimit";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}