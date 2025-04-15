package com.cordestitch.request.alteration;

import com.cordestitch.validation.alteration.ConditionalFieldValidation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConditionalFieldValidation
public class RescheduleOrCancelRequest {

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "Start time cannot be null")
    private LocalTime startTime;

    @NotNull(message = "End time cannot be null")
    private LocalTime endTime;

    @NotNull(message = "Slot date cannot be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate slotDate;

    @NotNull(message = "Reschedule flag must be specified")
    private Boolean reschedule;

    private LocalTime newStartTime;

    private LocalTime newEndTime;

    @FutureOrPresent(message = "New Slot date must be a future date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate newSlotDate;

}
