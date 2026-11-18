package com.umadjo.api.infra.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Limita o tamanho de uma String em bytes (UTF-8), e não em caracteres.
 * Necessário para senhas, pois o BCrypt aceita no máximo 72 bytes.
 */
@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface MaxBytes {

    int value();

    String message() default "Valor excede o tamanho máximo permitido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
