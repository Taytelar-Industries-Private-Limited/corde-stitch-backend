package com.cordestitch.request.alteration;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSlotRequest {

    @NotNull(message = "User ID is required")
    @NotEmpty(message = "User ID cannot be empty")
    private String userId;

    @NotNull(message = "Order item IDs are required")
    @Size(min = 1, message = "At least one order item ID is required")
    private List<String> orderItemIds;

    @NotNull(message = "Slot date is required")
    @FutureOrPresent(message = "Slot date must be today or a future date")
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}