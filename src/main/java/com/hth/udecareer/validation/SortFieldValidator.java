package com.hth.udecareer.validation;

import com.hth.udecareer.annotation.validation.ValidSortField;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

/**
 * Validator cho annotation @ValidSortField
 */
public class SortFieldValidator implements ConstraintValidator<ValidSortField, String> {

    // Whitelist các field được phép sort (mapping với QuizDto properties)
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",           // Quiz ID
            "name",         // Quiz name
            "timeLimit",    // Time limit in minutes
            "slug",         // Post slug
            "postId",       // Post ID
            "postTitle"     // Post title
    );

    @Override
    public void initialize(ValidSortField constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (ObjectUtils.isEmpty(value)) {
            return true;
        }

        String[] parts = value.split(",");

        if (parts.length == 0 || parts.length > 2) {
            return false;
        }

        String field = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            return false;
        }

        if (parts.length == 2) {
            String direction = parts[1].trim().toLowerCase();
            if (!direction.equals("asc") && !direction.equals("desc")) {
                return false;
            }
        }

        return true;
    }
}

