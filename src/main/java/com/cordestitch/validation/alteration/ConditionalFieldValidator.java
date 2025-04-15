package com.cordestitch.validation.alteration;

import com.cordestitch.request.alteration.RescheduleOrCancelRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConditionalFieldValidator implements ConstraintValidator<ConditionalFieldValidation, RescheduleOrCancelRequest> {

    @Override
    public boolean isValid(RescheduleOrCancelRequest request, ConstraintValidatorContext context) {
        if (Boolean.TRUE.equals(request.getReschedule())) {
            boolean hasError = false;

            if (request.getNewStartTime() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New Start Time cannot be null")
                        .addPropertyNode("newStartTime")
                        .addConstraintViolation();
                hasError = true;
            }

            if (request.getNewEndTime() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New End Time cannot be null")
                        .addPropertyNode("newEndTime")
                        .addConstraintViolation();
                hasError = true;
            }

            if (request.getNewSlotDate() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New Slot date cannot be null")
                        .addPropertyNode("newSlotDate")
                        .addConstraintViolation();
                hasError = true;
            }

            return !hasError;
        } else {
            boolean hasNewFieldError = false;

            if (request.getNewStartTime() != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New Start Time should not be specified when not rescheduling")
                        .addPropertyNode("newStartTime")
                        .addConstraintViolation();
                hasNewFieldError = true;
            }

            if (request.getNewEndTime() != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New End Time should not be specified when not rescheduling")
                        .addPropertyNode("newEndTime")
                        .addConstraintViolation();
                hasNewFieldError = true;
            }

            if (request.getNewSlotDate() != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New Slot date should not be specified when not rescheduling")
                        .addPropertyNode("newSlotDate")
                        .addConstraintViolation();
                hasNewFieldError = true;
            }

            return !hasNewFieldError;
        }
    }
}
