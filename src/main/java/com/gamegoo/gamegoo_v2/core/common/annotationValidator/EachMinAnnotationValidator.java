package com.gamegoo.gamegoo_v2.core.common.annotationValidator;

import com.gamegoo.gamegoo_v2.core.common.annotation.EachMin;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class EachMinAnnotationValidator implements ConstraintValidator<EachMin, List<? extends Number>> {

    private int value;
    private String message;

    @Override
    public void initialize(EachMin constraintAnnotation) {
        this.value = constraintAnnotation.value();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(List<? extends Number> value, ConstraintValidatorContext context) {
        // null 또는 비어 있는 리스트는 유효
        if (value == null || value.isEmpty()) {
            return true;
        }

        for (Number num : value) {
            if (num == null || num.doubleValue() < this.value) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }

}
