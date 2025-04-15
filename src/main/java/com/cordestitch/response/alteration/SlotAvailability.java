package com.cordestitch.response.alteration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotAvailability {

    private String slotTime;
    private boolean available;
}