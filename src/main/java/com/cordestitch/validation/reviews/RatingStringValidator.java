package com.cordestitch.validation.reviews;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RatingStringValidator implements ConstraintValidator<ValidRating, String> {

    @Override
    public boolean isValid(String rating, ConstraintValidatorContext context) {
        if (rating == null || rating.trim().isEmpty()) {
            return false;
        }

        try {
            int ratingValue = Integer.parseInt(rating);
            return ratingValue >= 1 && ratingValue <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
