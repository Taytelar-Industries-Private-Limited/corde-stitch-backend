package com.cordestitch.response.alteration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookedSlotResponse {

    private List<BookedSlotTimes> bookedSlotTimes;
}