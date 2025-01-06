package com.gamegoo.gamegoo_v2.core.common.annotationValidator;

import com.gamegoo.gamegoo_v2.core.common.annotation.NotDuplicated;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotDuplicatedAnnotationValidator implements ConstraintValidator<NotDuplicated, List<?>> {

    @Override
    public void initialize(NotDuplicated constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        // 값이 null이면 검증 통과 처리
        if (value == null) {
            return true;
        }

        // Set으로 변환하여 size 비교
        Set<?> set = new HashSet<>(value);
        return (set.size() == value.size());
    }


}
