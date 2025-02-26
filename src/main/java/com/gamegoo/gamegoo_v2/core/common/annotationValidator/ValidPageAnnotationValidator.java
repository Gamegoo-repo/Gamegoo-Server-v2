package com.gamegoo.gamegoo_v2.core.common.annotationValidator;

import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPageAnnotationValidator implements ConstraintValidator<ValidPage, Integer> {

    @Override
    public void initialize(ValidPage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value < 1) {
            return false;
        }

        return true;
    }

}
