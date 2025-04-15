package com.cordestitch.service.service.whatsapp;

import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.response.SuccessResponse;

public interface WhatsAppService {

    SuccessResponse notifyOrderConfirmation(String recipientNumber, String userName, String orderItemId, String orderDate);

    SuccessResponse notifyOrderCancellation(String recipientNumber, String userName, String orderItemId);

    SuccessResponse notifyOrderDelivered(String recipientNumber, String userName, String orderItemId, String orderDate);

    SuccessResponse generateOtp(String recipientNumber, String generatedOtp);

    SuccessResponse markAsRead(String messagingProduct, String status, String messageId);

    SuccessResponse sendCancellationReasons(String recipientNumber, String orderItemId);

    SuccessResponse cancelOrderActionRequest(String recipientNumber, String orderItemId);

    SuccessResponse notifyOrderCancelledFromWhatsapp(String recipientNumber);

    SuccessResponse sendSomeThingWentWrong(String recipientNumber, String message);

    SuccessResponse sendWelcomeMessage(String recipientNumber);

    SuccessResponse sendTextMessage(String recipientNumber, String messageBody);

    SuccessResponse sendCatalogAndSupport(String recipientNumber);

    SuccessResponse sendTopCategoryInteractiveList(String recipientNumber);

    SuccessResponse sendOrderStatusMessage(String recipientNumber, String userName, String orderItemId, String deliveryDate, DeliveryStatus deliveryStatus);

    SuccessResponse sendAlterationAppointmentMessage(String recipientNumber, String userName,String orderItemId,String appointmentDate,String appointmentTime);

    SuccessResponse cancelAppointmentActionRequest(String recipientNumber, String payLoadData);

    SuccessResponse sendAppointmentCancellationMessage(String recipientNumber, String userName,String appointmentDate,String appointmentTime);

    SuccessResponse sendAlterationReschedulingSlotDate(String recipientNumber, String payLoadData);

    SuccessResponse sendAlterationReschedulingSlotTimes(String recipientNumber, String payLoadData);

    SuccessResponse sendAppointmentRescheduleMessage(String recipientNumber, String userName, String orderItemId, String oldAppointmentDate, String oldAppointmentTime, String newAppointmentDate, String newAppointmentTime);

    SuccessResponse sendFitAppointmentMessage(String recipientNumber, String userName, String appointmentDate, String appointmentTime);
}