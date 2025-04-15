package com.cordestitch.controller.whatsapp;

import com.cordestitch.request.whatsapp.MarkAsReadRequest;
import com.cordestitch.request.whatsapp.NotificationRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp/")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    /**
     * Notifies a user about their order confirmation via WhatsApp.
     * This API endpoint sends an order confirmation message to the user's WhatsApp number.
     * It takes a NotificationRequest object containing recipient details and order information.
     * If the notification is successfully sent, it returns a SuccessResponse confirming the action.
     *
     * @param notificationRequest The request object containing recipient number, username, order item ID, and order date.
     * @return A ResponseEntity containing a SuccessResponse confirming the notification.
     */
    @PostMapping("notifyOrderConfirmation")
    public ResponseEntity<SuccessResponse> notifyOrderConfirmation(@ModelAttribute NotificationRequest notificationRequest) {
        SuccessResponse response = whatsAppService.notifyOrderConfirmation(notificationRequest.getRecipientNumber(),
                notificationRequest.getUserName(),
                notificationRequest.getOrderItemId(),
                notificationRequest.getOrderDate());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    /**
     * Notifies a user about their order cancellation via WhatsApp.
     * This API endpoint sends an order cancellation message to the user's WhatsApp number.
     * It takes a NotificationRequest object containing recipient details and order information.
     * If the notification is successfully sent, it returns a SuccessResponse confirming the action.
     *
     * @param cancelNotificationRequest The request object containing recipient number, username, and order item ID.
     * @return A ResponseEntity containing a SuccessResponse confirming the notification.
     */
    @PostMapping("notifyOrderCancellation")
    public ResponseEntity<SuccessResponse> notifyOrderCancellation(@ModelAttribute NotificationRequest cancelNotificationRequest) {
        SuccessResponse response = whatsAppService.notifyOrderCancellation(cancelNotificationRequest.getRecipientNumber(),
                cancelNotificationRequest.getUserName(),
                cancelNotificationRequest.getOrderItemId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Notifies a user about their order delivery via WhatsApp.
     * This API endpoint sends an order delivery confirmation message to the user's WhatsApp number.
     * It takes a NotificationRequest object containing recipient details and order information.
     * If the notification is successfully sent, it returns a SuccessResponse confirming the action.
     *
     * @param deliveredNotificationRequest The request object containing recipient number, username, order item ID, and order date.
     * @return A ResponseEntity containing a SuccessResponse confirming the notification.
     */
    @PostMapping("notifyOrderDelivered")
    public ResponseEntity<SuccessResponse> notifyOrderDelivered(@ModelAttribute NotificationRequest deliveredNotificationRequest) {
        SuccessResponse response = whatsAppService.notifyOrderDelivered(deliveredNotificationRequest.getRecipientNumber(),
                deliveredNotificationRequest.getUserName(),
                deliveredNotificationRequest.getOrderItemId(),
                deliveredNotificationRequest.getOrderDate());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Generates and sends an OTP to the user's phone number via WhatsApp.
     * This API endpoint sends a one-time password (OTP) to the specified recipient number.
     * The OTP can be used for authentication or verification purposes.
     * If the OTP is successfully sent, it returns a SuccessResponse confirming the action.
     *
     * @param recipientNumber The phone number to which the OTP should be sent.
     * @param generatedOtp    The OTP that will be sent to the user.
     * @return A ResponseEntity containing a SuccessResponse confirming the OTP delivery.
     */
    @PostMapping("generateOtp")
    public ResponseEntity<SuccessResponse> generateOtp(@RequestParam String recipientNumber, @RequestParam String generatedOtp) {
        SuccessResponse response = whatsAppService.generateOtp(recipientNumber, generatedOtp);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Marks a WhatsApp message as read.
     * This API endpoint updates the status of a message to "read" in the WhatsApp system.
     * It takes a MarkAsReadRequest object containing the messaging product, status, and message ID.
     * If the update is successful, it returns a SuccessResponse confirming the action.
     *
     * @param markAsReadRequest The request object containing messaging product, status, and message ID.
     * @return A ResponseEntity containing a SuccessResponse confirming the message has been marked as read.
     */
    @PostMapping("markAsRead")
    public ResponseEntity<SuccessResponse> markAsRead(@ModelAttribute MarkAsReadRequest markAsReadRequest) {
        SuccessResponse response = whatsAppService.markAsRead(markAsReadRequest.getMessagingProduct(), markAsReadRequest.getStatus(), markAsReadRequest.getMessageId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Sends order cancellation reasons to the user via WhatsApp.
     * This API endpoint sends a message to the user's WhatsApp number containing reasons for order cancellation.
     * It takes a NotificationRequest object containing recipient details and order item ID.
     * If the message is successfully sent, it returns a SuccessResponse confirming the action.
     *
     * @param interActiveRequest The request object containing recipient number and order item ID.
     * @return A ResponseEntity containing a SuccessResponse confirming the cancellation reasons were sent.
     */
    @PostMapping("sendCancellationReasons")
    public ResponseEntity<SuccessResponse> sendCancellationReasons(@ModelAttribute NotificationRequest interActiveRequest) {
        SuccessResponse response = whatsAppService.sendCancellationReasons(interActiveRequest.getRecipientNumber(), interActiveRequest.getOrderItemId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Sends a cancellation action request to the user via WhatsApp.
     * This API endpoint allows sending a request to confirm an order cancellation.
     * It takes a NotificationRequest object containing recipient details and order item ID.
     * If the request is successfully sent, it returns a SuccessResponse confirming the action.
     *
     * @param cancelOrderActionRequest The request object containing recipient number and order item ID.
     * @return A ResponseEntity containing a SuccessResponse confirming the cancellation request was sent.
     */
    @PostMapping("cancelOrderActionRequest")
    public ResponseEntity<SuccessResponse> cancelOrderActionRequest(@ModelAttribute NotificationRequest cancelOrderActionRequest) {
        SuccessResponse response = whatsAppService.cancelOrderActionRequest(cancelOrderActionRequest.getRecipientNumber(), cancelOrderActionRequest.getOrderItemId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Notifies the system that an order was cancelled via WhatsApp.
     * This API endpoint processes an order cancellation request that originated from WhatsApp.
     * It takes a NotificationRequest object containing the recipient's phone number.
     * If the notification is successfully processed, it returns a SuccessResponse confirming the action.
     *
     * @param cancelNotificationRequest The request object containing the recipient's phone number.
     * @return A ResponseEntity containing a SuccessResponse confirming the order cancellation.
     */
    @PostMapping("notifyOrderCancelledFromWhatsapp")
    public ResponseEntity<SuccessResponse> notifyOrderCancelledFromWhatsapp(@ModelAttribute NotificationRequest cancelNotificationRequest) {
        SuccessResponse response = whatsAppService.notifyOrderCancelledFromWhatsapp(cancelNotificationRequest.getRecipientNumber());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}