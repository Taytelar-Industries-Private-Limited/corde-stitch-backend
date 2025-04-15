package com.cordestitch.service.service.alteration;

import com.cordestitch.request.alteration.BookMeasurementSlotRequest;
import com.cordestitch.request.alteration.BookSlotRequest;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.alteration.RescheduleOrCancelRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.alteration.AlterationResponse;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.SlotTimes;


public interface AlterationService {
    AlterationResponse getOrderItems(String userId, String phoneNumber, String emailAddress);

    SlotTimes getSlotTimes(GetSlotTimesRequest request);

    SuccessResponse bookSlotTimes(BookSlotRequest request);

    SuccessResponse rescheduleOrCancelSlotTimes(RescheduleOrCancelRequest request);

    BookedSlotResponse getBookedSlotTimes(String userId);

    SuccessResponse bookMeasurementSlot(BookMeasurementSlotRequest request);
}