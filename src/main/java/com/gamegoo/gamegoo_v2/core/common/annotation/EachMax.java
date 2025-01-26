package com.gamegoo.gamegoo_v2.core.common.annotation;

import com.gamegoo.gamegoo_v2.core.common.annotationValidator.EachMaxAnnotationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EachMaxAnnotationValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EachMax {

    String message() default "리스트의 각 요소는 유효하지 않은 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int value() default Integer.MAX_VALUE;

}
