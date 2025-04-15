package com.cordestitch.response.alteration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotTimes {

    private LocalDate todayDate;
    private List<SlotAvailability> availableSlots;
}
