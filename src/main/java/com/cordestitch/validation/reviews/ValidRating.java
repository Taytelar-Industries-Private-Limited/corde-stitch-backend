package com.cordestitch.validation.reviews;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RatingStringValidator.class)
public @interface ValidRating {
    String message() default "Invalid rating value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
