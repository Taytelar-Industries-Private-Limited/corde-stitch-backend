package com.cordestitch.validation.alteration;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ConditionalFieldValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalFieldValidation {
    String message() default "Validation failed for conditional fields.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
