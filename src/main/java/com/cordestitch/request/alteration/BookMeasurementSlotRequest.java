package com.cordestitch.request.alteration;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookMeasurementSlotRequest {

    @NotNull(message = "User ID is required")
    @NotEmpty(message = "User ID cannot be empty")
    private String userId;

    @NotNull(message = "Slot date is required")
    @FutureOrPresent(message = "Slot date must be today or a future date")
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Address ID is required")
    @NotEmpty(message = "Address ID cannot be empty")
    private String addressId;

}