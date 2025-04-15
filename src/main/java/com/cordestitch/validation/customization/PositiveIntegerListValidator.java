package com.cordestitch.validation.customization;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class PositiveIntegerListValidator implements ConstraintValidator<PositiveIntegerList, List<Integer>> {

    @Override
    public boolean isValid(List<Integer> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        }
        return values.stream().allMatch(value -> value != null && value > 0);
    }
}
