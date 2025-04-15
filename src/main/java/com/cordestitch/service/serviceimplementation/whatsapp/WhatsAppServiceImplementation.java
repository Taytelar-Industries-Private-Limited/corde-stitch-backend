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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
    public SuccessResponse sendOrderStatusMessage(String recipientNumber, String userName, String orderItemId, String deliveryDate, DeliveryStatus deliveryStatus) {

        String templateName = whatsAppTemplateHelper.sendTemplateByDeliveryStatus(deliveryStatus);

        WhatsAppMessageRequest notifyOrderStatusRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, templateName, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createOrderStatusTemplate(userName, orderItemId, deliveryDate, deliveryStatus));
        log.info("Order status message request {}", notifyOrderStatusRequest);

        return sendWhatsAppMessage(notifyOrderStatusRequest);
    }

    @Override
    public SuccessResponse sendAlterationAppointmentMessage(String recipientNumber, String userName, String orderItemId, String appointmentDate, String appointmentTime) {

        WhatsAppMessageRequest sendAlterationAppointment = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.ALTERATION_APPOINTMENT_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createAlterationAppointmentTemplate(userName, orderItemId, appointmentDate, appointmentTime));
        log.info("Notify Alteration confirmation message request {}", sendAlterationAppointment);

        return sendWhatsAppMessage(sendAlterationAppointment);
    }

    @Override
    public SuccessResponse cancelAppointmentActionRequest(String recipientNumber, String payLoadData) {

        List<InteractiveButtonRequest> buttons = whatsAppInteractiveHelper.getCancellationButtons(payLoadData, WhatsAppConstants.YES_CANCEL_BUTTON, WhatsAppConstants.DO_NOT_CANCEL_BUTTON);

        WhatsAppMessageRequest cancelActionRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                null,
                WhatsAppConstants.CANCEL_APPOINTMENT_ACTION_BODY_TEXT,
                null,
                WhatsAppConstants.BUTTON,
                null,
                buttons);

        return sendWhatsAppMessage(cancelActionRequest);
    }

    @Override
    public SuccessResponse sendAppointmentCancellationMessage(String recipientNumber, String userName, String appointmentDate, String appointmentTime) {

        WhatsAppMessageRequest sendAlterationAppointment = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.APPOINTMENT_CANCELLATION_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createAppointmentCancellationTemplate(userName, appointmentDate, appointmentTime));
        log.info("Notify appointment cancellation message request {}", sendAlterationAppointment);

        return sendWhatsAppMessage(sendAlterationAppointment);
    }

    @Override
    public SuccessResponse sendAlterationReschedulingSlotDate(String recipientNumber, String payLoadData) {

        List<SectionRequest> sections = getAvailableDateSlotSections(recipientNumber, payLoadData);

        WhatsAppMessageRequest slotDateRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                WhatsAppConstants.RESCHEDULE_APPOINTMENT_DATE_INTERACTIVE_HEADER_TEXT,
                WhatsAppConstants.RESCHEDULE_APPOINTMENT_DATE_INTERACTIVE_BODY_TEXT,
                WhatsAppConstants.RESCHEDULE_APPOINTMENT_DATE_INTERACTIVE_BUTTON_TEXT,
                WhatsAppConstants.LIST,
                sections,
                null);

        return sendWhatsAppMessage(slotDateRequest);
    }

    @Override
    public SuccessResponse sendAlterationReschedulingSlotTimes(String recipientNumber, String payLoadData) {

        List<SectionRequest> sections = getAvailableTimeSlotSections(payLoadData, recipientNumber);

        WhatsAppMessageRequest slotTimeRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest(recipientNumber,
                WhatsAppConstants.RESCHEDULE_APPOINTMENT_TIME_INTERACTIVE_HEADER_TEXT,
                WhatsAppConstants.RESCHEDULE_APPOINTMENT_TIME_INTERACTIVE_BODY_TEXT,
                WhatsAppConstants.RESCHEDULE_APPOINTMENT_TIME_INTERACTIVE_BUTTON_TEXT,
                WhatsAppConstants.LIST,
                sections,
                null);

        return sendWhatsAppMessage(slotTimeRequest);
    }

    @Override
    public SuccessResponse sendAppointmentRescheduleMessage(String recipientNumber, String userName, String orderItemId, String oldAppointmentDate, String oldAppointmentTime, String newAppointmentDate, String newAppointmentTime) {

        WhatsAppMessageRequest sendAppointmentRescheduleRequest = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.ALTERATION_RESCHEDULE_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createAppointmentRescheduleTemplate(userName, orderItemId, oldAppointmentDate, oldAppointmentTime, newAppointmentTime, newAppointmentDate));
        log.info("Notify Reschedule confirmation message request {}", sendAppointmentRescheduleRequest);

        return sendWhatsAppMessage(sendAppointmentRescheduleRequest);
    }

    @Override
    public SuccessResponse sendFitAppointmentMessage(String recipientNumber, String userName, String appointmentDate, String appointmentTime) {

        WhatsAppMessageRequest sendFitAppointment = whatsAppTemplateHelper.createMessageRequest(recipientNumber, WhatsAppConstants.FIT_APPOINTMENT_TEMPLATE, WhatsAppConstants.EN_US, whatsAppTemplateHelper.createFitAppointmentTemplate(userName, appointmentDate, appointmentTime));
        log.info("Notify fit appointment message request {}", sendFitAppointment);

        return sendWhatsAppMessage(sendFitAppointment);
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


    public List<SectionRequest> getAvailableDateSlotSections(String recipientNumber, String payLoadData) {

        List<LocalDate> availableSlotDates = whatsAppInteractiveHelper.fetchAvailableSlotDates(recipientNumber, payLoadData);
        List<RowRequest> rows = new ArrayList<>();

        if (availableSlotDates.isEmpty()) {
            sendTextMessage(recipientNumber, WhatsAppConstants.TIME_SLOT_DATE_NOT_AVAILABLE);
        }

        for (LocalDate availableSlotDate : availableSlotDates) {
            rows.add(whatsAppInteractiveHelper.createRow(payLoadData + "_N_" + availableSlotDate, String.valueOf(availableSlotDate)));
        }

        SectionRequest section = whatsAppInteractiveHelper.createSection(WhatsAppConstants.TITTLE, rows);
        return Collections.singletonList(section);
    }


    public List<SectionRequest> getAvailableTimeSlotSections(String payLoadData, String recipientNumber) {

        List<String> availableSlotTimes = whatsAppInteractiveHelper.fetchAvailableSlotTimes(payLoadData);
        List<RowRequest> rows = new ArrayList<>();

        if (availableSlotTimes.isEmpty()) {
            sendTextMessage(recipientNumber, WhatsAppConstants.TIME_SLOT_TIME_NOT_AVAILABLE);
        }
        for (String availableSlotTime : availableSlotTimes) {
            rows.add(whatsAppInteractiveHelper.createRow(payLoadData + "_NT_" + availableSlotTime, String.valueOf(availableSlotTime)));
        }
        SectionRequest section = whatsAppInteractiveHelper.createSection(WhatsAppConstants.TITTLE, rows);

        return Collections.singletonList(section);
    }
}