package com.cordestitch.controller.alteration;

import com.cordestitch.request.alteration.BookMeasurementSlotRequest;
import com.cordestitch.request.alteration.BookSlotRequest;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.alteration.RescheduleOrCancelRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.alteration.AlterationResponse;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.SlotTimes;
import com.cordestitch.service.service.alteration.AlterationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/alteration")
@RequiredArgsConstructor
public class AlterationController {

    private final AlterationService alterationService;

    /**
     * Retrieves order items for a specific user.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @param phoneNumber  (Optional) The phone number of the user to filter order items.
     * @param emailAddress (Optional) The email address of the user to filter order items.
     * @return ResponseEntity containing the list of order items associated with the user.
     */
    @Validated
    @GetMapping("/getOrderItems")
    public ResponseEntity<AlterationResponse> getOrderItems(HttpServletRequest request,
                                                            @RequestParam(required = false) String phoneNumber,
                                                            @RequestParam(required = false) String emailAddress) {
        String userId = (String) request.getAttribute("userId");
        AlterationResponse response = alterationService.getOrderItems(userId, phoneNumber, emailAddress);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves available slot times for alterations based on the given request.
     *
     * @param request The request containing necessary parameters to get available slot times.
     * @return ResponseEntity containing the available slot times for alterations.
     */
    @PostMapping("/getSlotTimes")
    public ResponseEntity<SlotTimes> getSlotTimes(@Valid @RequestBody GetSlotTimesRequest request) {
        SlotTimes slotTimes = alterationService.getSlotTimes(request);
        return ResponseEntity.status(HttpStatus.OK).body(slotTimes);
    }

    /**
     * Books the selected slot times for alterations based on the provided request.
     *
     * @param request The request containing the details of the slot to be booked.
     * @return ResponseEntity indicating the success or failure of the booking operation.
     */
    @PostMapping("/bookSlotTimes")
    public ResponseEntity<SuccessResponse> bookSlotTimes(@Valid @RequestBody BookSlotRequest request) {
        SuccessResponse response = alterationService.bookSlotTimes(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Retrieves the booked slot times for a specific user.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @return ResponseEntity containing the list of booked slot times for the user.
     */
    @GetMapping("/getBookedSlotTimes")
    public ResponseEntity<BookedSlotResponse> getBookedSlotTimes(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        BookedSlotResponse slotTimes = alterationService.getBookedSlotTimes(userId);
        return ResponseEntity.status(HttpStatus.OK).body(slotTimes);
    }

    /**
     * API endpoint to reschedule or cancel a previously booked slot time for a delivery or alteration service.

     * This endpoint allows users to either reschedule their booked slot to a different time or cancel their existing slot.
     * It accepts a request payload containing the necessary details to identify the booking,
     * and whether the user wants to reschedule to a new slot or cancel it.
     *
     * @param request The request body containing the rescheduling or cancel details like order item IDs and the slot to reschedule or cancel.
     * @return A `SuccessResponse` with the status of the rescheduling or cancellation operation.
     */
    @PostMapping("/rescheduleOrCancelSlotTimes")
    public ResponseEntity<SuccessResponse> rescheduleOrCancelSlotTimes(@Validated @RequestBody RescheduleOrCancelRequest request) {
        SuccessResponse response = alterationService.rescheduleOrCancelSlotTimes(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * API endpoint to book a measurement slot for a user.

     * This endpoint allows users to book a time slot to collect new measurements
     * for their alteration or tailoring requests. The request requires the user ID,
     * slot date, start and end time, and the address where the measurement will be taken.
     *
     * @param request The request body containing the details for booking the measurement slot.
     *                The following fields are required:
     *                - userId: The unique ID of the user booking the slot.
     *                - slotDate: The date of the measurement slot (must be today or a future date).
     *                - startTime: The start time of the slot.
     *                - endTime: The end time of the slot.
     *                - addressId: The ID of the address where the measurement will take place.
     * @return A response containing the success message and the details of the booked slot.
     *         Returns HTTP status 200 (OK) if the slot is successfully booked.
     *
     * @throws com.cordestitch.exception.alteration.SlotAlreadyBookedException if the selected slot is already booked.

     * Status codes:
     * - 200 OK: Slot successfully booked.
     * - 400 Bad Request: Invalid slot time or data format in the request.
     * - 409 Conflict: The selected slot is already booked.
     */
    @PostMapping("/bookMeasurementSlot")
    public ResponseEntity<SuccessResponse> bookMeasurementSlot(@Valid @RequestBody BookMeasurementSlotRequest request) {
        SuccessResponse successResponse = alterationService.bookMeasurementSlot(request);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }
}