package com.cordestitch.response.alteration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.response.user.AddressResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookedSlotTimes {

    private String slotId;
    private LocalDate bookedSlotDate;
    @JsonFormat(pattern = "hh:mma", timezone = "Asia/Kolkata")
    private LocalTime startTime;
    @JsonFormat(pattern = "hh:mma", timezone = "Asia/Kolkata")
    private LocalTime endTime;
    private AddressResponse addressResponse;
    private List<OrderItemResponse> orderItemResponses;
    private Boolean isNewMeasurement;
}