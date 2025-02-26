package com.gamegoo.gamegoo_v2.core.common.annotation;

import com.gamegoo.gamegoo_v2.core.common.annotationValidator.ValidCursorAnnotationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidCursorAnnotationValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCursor {

    String message() default "커서는 1 이상의 값이어야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
