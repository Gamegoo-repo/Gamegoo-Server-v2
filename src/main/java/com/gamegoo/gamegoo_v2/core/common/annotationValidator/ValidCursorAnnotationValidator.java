package com.gamegoo.gamegoo_v2.core.common.annotationValidator;

import com.gamegoo.gamegoo_v2.core.common.annotation.ValidCursor;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCursorAnnotationValidator implements ConstraintValidator<ValidCursor, Long> {

    @Override
    public void initialize(ValidCursor constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value != null && value < 1) {
            return false;
        }

        return true;
    }

}
