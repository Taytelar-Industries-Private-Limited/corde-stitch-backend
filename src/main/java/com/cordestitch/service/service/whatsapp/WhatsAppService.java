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

    SuccessResponse notifyOrderCancelledFromWhatsapp(String recipientNumber);

    SuccessResponse sendSomeThingWentWrong(String recipientNumber, String message);

    SuccessResponse sendWelcomeMessage(String recipientNumber);

    SuccessResponse sendTextMessage(String recipientNumber, String messageBody);

    SuccessResponse sendCatalogAndSupport(String recipientNumber);

    SuccessResponse sendTopCategoryInteractiveList(String recipientNumber);

    SuccessResponse cancelOrderActionRequest(String recipientNumber, String orderItemId);

    SuccessResponse sendOrderStatusMessage(String recipientNumber, String userName, String orderItemId, String deliveryDate, DeliveryStatus deliveryStatus);
}