package com.cordestitch.service.serviceimplementation.whatsapp;

import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.util.WhatsAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WhatsAppServiceImplementation implements WhatsAppService {

    @Value("${whatsapp.api.baseApiUrl}")
    private String baseApiUrl;

    @Value("${whatsapp.api.token}")
    private String accessToken;

    @Value("${whatsapp.api.whatsapp-phone-id}")
    private String whatsappPhoneId;

    private final RestTemplate restTemplate;

    private final WhatsAppTemplateHelper whatsAppTemplateHelper;

    private final WhatsAppInteractiveHelper whatsAppInteractiveHelper;

    @Override
    public SuccessResponse notifyOrderConfirmation(String recipientNumber, String userName, String orderItemId, String orderDate) {
        WhatsAppMessageRequest notifyOrderConfirmRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.ORDER_CONFIRM_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createOrderConfirmationTemplate(userName, orderItemId, orderDate));
        log.info("Notify order confirm message request {}", notifyOrderConfirmRequest);
        return sendWhatsAppMessage(notifyOrderConfirmRequest);
    }

    @Override
    public SuccessResponse notifyOrderCancellation(String recipientNumber, String userName, String orderItemId) {
        WhatsAppMessageRequest notifyOrderCancelRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.ORDER_CANCEL_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createOrderCancellationTemplate(userName, orderItemId));
        log.info("Notify Order cancel message request {}", notifyOrderCancelRequest);
        return sendWhatsAppMessage(notifyOrderCancelRequest);
    }

    @Override
    public SuccessResponse notifyOrderDelivered(String recipientNumber, String userName, String orderItemId, String orderDate) {
        WhatsAppMessageRequest notifyOrderCancelRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.ORDER_DELIVERED_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createOrderDeliveredTemplate(userName, orderItemId, orderDate));
        log.info("Notify order delivered message request {}", notifyOrderCancelRequest);
        return sendWhatsAppMessage(notifyOrderCancelRequest);
    }

    @Override
    public SuccessResponse generateOtp(String recipientNumber, String generatedOtp) {
        WhatsAppMessageRequest generateOtpRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.OTP_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createOtpTemplate(generatedOtp));
        log.info("Generate OTP message request{}", generateOtpRequest);
        return sendWhatsAppMessage(generateOtpRequest);
    }

    @Override
    public SuccessResponse markAsRead(String messagingProduct, String status, String messageId) {
        MarkAsReadRequest markAsReadRequest = whatsAppTemplateHelper.createMarkAsReadRequest(messagingProduct, status, messageId);
        log.info("Mark as read request {}", markAsReadRequest);
        sendHttpPostRequest(baseApiUrl + whatsappPhoneId + WhatsAppConstants.MESSAGE, markAsReadRequest, String.class);
        return new SuccessResponse(WhatsAppConstants.MESSAGE_SENT, HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse sendCancellationReasons(String recipientNumber, String orderItemId) {
        List<SectionRequest> sections = whatsAppInteractiveHelper.getCancellationReasonSections(orderItemId);
        WhatsAppMessageRequest cancellationReasonRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                WhatsAppConstants.CANCEL_ORDER_INTERACTIVE_HEADER_TEXT,
                WhatsAppConstants.CANCEL_ORDER_INTERACTIVE_BODY_TEXT,
                WhatsAppConstants.CANCEL_ORDER_INTERACTIVE_BUTTON_TEXT,
                WhatsAppConstants.LIST,
                sections,
                null);
        return sendWhatsAppMessage(cancellationReasonRequest);
    }

    @Override
    public SuccessResponse notifyOrderCancelledFromWhatsapp(String recipientNumber) {
        WhatsAppMessageRequest notifyOrderCancelRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.ORDER_CANCEL_FROM_WHATSAPP_TEMPLATE, WhatsAppConstants.EN_US, null);
        log.info("Notify Order cancel message request from whatsapp {}", notifyOrderCancelRequest);
        return sendWhatsAppMessage(notifyOrderCancelRequest);
    }

    @Override
    public SuccessResponse sendSomeThingWentWrong(String recipientNumber, String message) {
        WhatsAppMessageRequest sendSomeThingWrongRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.SOMETHING_WENT_WRONG_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createSomeThingWentWrongTemplate(message));
        log.info("Send something went wrong message {}", sendSomeThingWrongRequest);
        return sendWhatsAppMessage(sendSomeThingWrongRequest);
    }

    @Override
    public SuccessResponse sendWelcomeMessage(String recipientNumber) {
        List<InteractiveButtonRequest> buttons = whatsAppInteractiveHelper.createWelcomeMessageButtons();
        WhatsAppMessageRequest sendWelcomeMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                null,
                WhatsAppConstants.WELCOME_MESSAGE,
                null,
                WhatsAppConstants.BUTTON,
                null,
                buttons);
        return sendWhatsAppMessage(sendWelcomeMessageRequest);
    }

    @Override
    public SuccessResponse sendTextMessage(String recipientNumber, String messageBody) {
        WhatsAppMessageRequest textMessageRequest = whatsAppTemplateHelper.createTextMessageRequest(recipientNumber, messageBody);
        return sendWhatsAppMessage(textMessageRequest);

    }

    @Override
    public SuccessResponse sendCatalogAndSupport(String recipientNumber) {
        List<InteractiveButtonRequest> buttons = whatsAppInteractiveHelper.getCatalogAndSupportSections();
        WhatsAppMessageRequest sendCatalogRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                null,
                WhatsAppConstants.MOVE_FORWARD_MESSAGE,
                null,
                WhatsAppConstants.BUTTON,
                null,
                buttons);
        return sendWhatsAppMessage(sendCatalogRequest);

    }

    @Override
    public SuccessResponse sendTopCategoryInteractiveList(String recipientNumber) {
        List<SectionRequest> sections = whatsAppInteractiveHelper.createTopCategorySections();
        WhatsAppMessageRequest sendTopCategoryRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                WhatsAppConstants.TOP_CATEGORY_INTERACTIVE_HEADER_TEXT,
                WhatsAppConstants.TOP_CATEGORY_INTERACTIVE_BODY_TEXT,
                WhatsAppConstants.TOP_CATEGORY_INTERACTIVE_BUTTON_TEXT,
                WhatsAppConstants.LIST,
                sections,
                null);
        return sendWhatsAppMessage(sendTopCategoryRequest);
    }

    @Override
    public SuccessResponse cancelOrderActionRequest(String recipientNumber, String orderItemId) {
        List<InteractiveButtonRequest> buttons = whatsAppInteractiveHelper.getCancellationButtons(orderItemId, WhatsAppConstants.YES_BUTTON, WhatsAppConstants.NO_BUTTON);
        WhatsAppMessageRequest cancelActionRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                null,
                WhatsAppConstants.CANCEL_ORDER_ACTION_BODY_TEXT,
                null,
                WhatsAppConstants.BUTTON,
                null,
                buttons);
        return sendWhatsAppMessage(cancelActionRequest);
    }
    @Override
    public SuccessResponse sendOrderStatusMessage(String recipientNumber, String userName, String orderItemId, String deliveryDate, DeliveryStatus deliveryStatus) {
        String templateName = whatsAppTemplateHelper.sendTemplateByDeliveryStatus(deliveryStatus);
        WhatsAppMessageRequest notifyOrderStatusRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, templateName, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createOrderStatusTemplate(userName, orderItemId, deliveryDate, deliveryStatus));
        log.info("Order status message request {}", notifyOrderStatusRequest);
        return sendWhatsAppMessage(notifyOrderStatusRequest);
    }

    public SuccessResponse sendWhatsAppMessage(WhatsAppMessageRequest messageRequest) {
        log.info("Sending WhatsApp message request: {}", messageRequest);
        sendHttpPostRequest(baseApiUrl + whatsappPhoneId + WhatsAppConstants.MESSAGE, messageRequest, String.class);
        return new SuccessResponse(WhatsAppConstants.MESSAGE_SENT, HttpStatus.OK.value());
    }

    public <T> void sendHttpPostRequest(String baseUrl, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Object> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForObject(baseUrl, httpEntity, responseType);
        } catch (Exception e) {
            log.error("Error sending WhatsApp message: {}", e.getMessage(), e);
        }
    }
}