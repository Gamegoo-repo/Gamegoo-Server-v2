package com.gamegoo.gamegoo_v2.core.common.annotationValidator;

import com.gamegoo.gamegoo_v2.core.common.annotation.ValidCursor;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class ValidCursorLocalDateTimeValidator implements ConstraintValidator<ValidCursor, LocalDateTime> {
    @Override
    public void initialize(ValidCursor constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        // 2020-01-01 이후의 값만 허용 (예시)
        return value.isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
    }
}
