package com.cordestitch.service.serviceimplementation.whatsapp;

import com.cordestitch.enums.ComponentType;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.ParameterType;
import com.cordestitch.request.webhook.TextRequest;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.util.WhatsAppConstants;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Objects.isNull;

@Component
public class WhatsAppTemplateHelper {

    public List<TemplateComponent> createOrderConfirmationTemplate(String userName, String confirmedOrderItemId, String orderDate) {

        TemplateComponent headerComponent = createComponent(ComponentType.HEADER, null, null);
        headerComponent.setParameters(Collections.singletonList(createParameter(ParameterType.IMAGE, WhatsAppConstants.IMAGE_URL)));

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, confirmedOrderItemId),
                createParameter(ParameterType.DATE_TIME, orderDate)));

        TemplateComponent trackOrderButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 0);
        trackOrderButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.TRACK_ORDER + confirmedOrderItemId)));

        TemplateComponent cancelOrderButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 1);
        cancelOrderButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.CANCEL_ORDER + confirmedOrderItemId)));

        return Arrays.asList(headerComponent, bodyComponent, trackOrderButtonComponent, cancelOrderButtonComponent);
    }

    public List<TemplateComponent> createOrderCancellationTemplate(String userName, String cancelledOrderId) {

        TemplateComponent headerComponent = createComponent(ComponentType.HEADER, null, null);
        headerComponent.setParameters(Collections.singletonList(createParameter(ParameterType.IMAGE, WhatsAppConstants.IMAGE_URL)));

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, cancelledOrderId)));

        return Arrays.asList(headerComponent, bodyComponent);
    }

    public List<TemplateComponent> createOrderDeliveredTemplate(String userName, String deliverOrderId, String orderDate) {

        TemplateComponent headerComponent = createComponent(ComponentType.HEADER, null, null);
        headerComponent.setParameters(Collections.singletonList(createParameter(ParameterType.IMAGE, WhatsAppConstants.IMAGE_URL)));

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, deliverOrderId),
                createParameter(ParameterType.DATE_TIME, orderDate)));

        return Arrays.asList(headerComponent, bodyComponent);
    }

    public List<TemplateComponent> createOtpTemplate(String generatedOtp) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Collections.singletonList(createParameter(ParameterType.TEXT, generatedOtp)));

        TemplateComponent buttonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.SUBSTRING_URL, 0);
        buttonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.TEXT, generatedOtp)));

        return Arrays.asList(buttonComponent, bodyComponent);
    }


    public WhatsAppMessageRequest createMessageRequest(String recipientNumber, String templateName, String templateLanguageCode, List<TemplateComponent> components) {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct(WhatsAppConstants.WHATSAPP);
        whatsAppMessageRequest.setTo(recipientNumber);
        whatsAppMessageRequest.setType(WhatsAppConstants.TEMPLATE);

        MessageTemplate messageTemplate = createTemplateRequest(components, templateName, templateLanguageCode);

        whatsAppMessageRequest.setTemplate(messageTemplate);
        return whatsAppMessageRequest;
    }

    public MessageTemplate createTemplateRequest(List<TemplateComponent> components, String templateName, String templateLanguageCode) {
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setName(templateName);
        messageTemplate.setLanguage(createTemplateLanguage(templateLanguageCode));
        messageTemplate.setComponents(components);
        return messageTemplate;
    }


    public TemplateLanguage createTemplateLanguage(String templateLanguageCode) {

        TemplateLanguage templateLanguage = new TemplateLanguage();
        templateLanguage.setCode(templateLanguageCode);
        templateLanguage.setPolicy(WhatsAppConstants.LANGUAGE_POLICY);
        return templateLanguage;
    }

    public TemplateComponent createComponent(ComponentType componentType, String subType, Integer index) {
        TemplateComponent component = new TemplateComponent();
        if (componentType.equals(ComponentType.HEADER) || componentType.equals(ComponentType.BODY) || componentType.equals(ComponentType.FOOTER)) {
            component.setType(componentType.getType());
            component.setParameters(new ArrayList<>());
        } else if (componentType.equals(ComponentType.BUTTON)) {
            component.setType(componentType.getType());
            component.setSubType(subType);
            component.setIndex(index);
            component.setParameters(new ArrayList<>());
        }
        return component;
    }

    public Parameter createParameter(ParameterType parameterType, String value) {
        Parameter parameter = new Parameter();
        switch (parameterType) {
            case TEXT:
                parameter.setType(parameterType.getType());
                parameter.setText(value);
                break;
            case DATE_TIME:
                parameter.setType(parameterType.getType());
                DateTimeParameter dateTimeParameter = new DateTimeParameter();
                dateTimeParameter.setFallbackValue(value);
                parameter.setDateTimeParameter(dateTimeParameter);
                break;
            case IMAGE:
                parameter.setType(parameterType.getType());
                ImageParameter imageParameter = new ImageParameter();
                imageParameter.setLink(value);
                parameter.setImageParameter(imageParameter);
                break;
            case PAYLOAD:
                parameter.setType(parameterType.getType());
                parameter.setPayload(value);
                break;
        }
        return parameter;
    }


    public MarkAsReadRequest createMarkAsReadRequest(String messagingProduct, String status, String messageId) {
        MarkAsReadRequest markAsReadRequest = new MarkAsReadRequest();
        markAsReadRequest.setMessageId(messageId);
        markAsReadRequest.setMessagingProduct(messagingProduct);
        markAsReadRequest.setStatus(status);
        return markAsReadRequest;
    }

    public WhatsAppMessageRequest createTextMessageRequest(String recipientNumber, String text) {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct(WhatsAppConstants.WHATSAPP);
        whatsAppMessageRequest.setTo(recipientNumber);
        whatsAppMessageRequest.setType(WhatsAppConstants.TEXT);

        TextRequest textRequest = new TextRequest();
        textRequest.setBody(text);
        whatsAppMessageRequest.setText(textRequest);

        return whatsAppMessageRequest;
    }

    public List<TemplateComponent> createSomeThingWentWrongTemplate(String message) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Collections.singletonList(
                createParameter(ParameterType.TEXT, message)));

        return List.of(bodyComponent);
    }

    public List<TemplateComponent> createOrderStatusTemplate(String userName, String orderItemId, String deliveryDate, DeliveryStatus deliveryStatus) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(createParameter(ParameterType.TEXT, userName));
        parameters.add(createParameter(ParameterType.TEXT, orderItemId));
        if (!isNull(deliveryDate) && EnumSet.of(DeliveryStatus.SHIPPED, DeliveryStatus.ORDER_CONFIRMED).contains(deliveryStatus))
            parameters.add(createParameter(ParameterType.DATE_TIME, deliveryDate));

        bodyComponent.setParameters(parameters);
        return List.of(bodyComponent);
    }

    public List<TemplateComponent> createAlterationAppointmentTemplate(String userName, String orderItemId, String appointmentDate, String appointmentTime) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, orderItemId),
                createParameter(ParameterType.TEXT, appointmentDate),
                createParameter(ParameterType.TEXT, appointmentTime)));

        TemplateComponent cancelAppointmentButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 1);
        cancelAppointmentButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.CANCEL_APPOINTMENT + orderItemId + "_" + appointmentDate + "_T_" + appointmentTime)));

        TemplateComponent rescheduleAppointmentButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 0);
        rescheduleAppointmentButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.RESCHEDULE_APPOINTMENT + orderItemId + "_" + appointmentDate + "_T_" + appointmentTime)));

        return Arrays.asList(bodyComponent, cancelAppointmentButtonComponent, rescheduleAppointmentButtonComponent);
    }

    public List<TemplateComponent> createAppointmentCancellationTemplate(String userName, String appointmentDate, String appointmentTime) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, appointmentDate),
                createParameter(ParameterType.TEXT, appointmentTime)));

        return List.of(bodyComponent);
    }

    public List<TemplateComponent> createAppointmentRescheduleTemplate(String userName, String orderItemId, String oldAppointmentDate, String oldAppointmentTime, String newAppointmentTime, String newAppointmentDate) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, orderItemId),
                createParameter(ParameterType.TEXT, oldAppointmentDate),
                createParameter(ParameterType.TEXT, oldAppointmentTime),
                createParameter(ParameterType.TEXT, newAppointmentDate),
                createParameter(ParameterType.TEXT, newAppointmentTime)));

        TemplateComponent rescheduleAppointmentButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 0);
        rescheduleAppointmentButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.RESCHEDULE_APPOINTMENT + orderItemId + "_" + newAppointmentDate + "_T_" + newAppointmentTime)));

        TemplateComponent cancelAppointmentButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 1);
        cancelAppointmentButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.CANCEL_APPOINTMENT + orderItemId + "_" + newAppointmentDate + "_T_" + newAppointmentTime)));

        return Arrays.asList(bodyComponent, rescheduleAppointmentButtonComponent, cancelAppointmentButtonComponent);
    }

    public List<TemplateComponent> createFitAppointmentTemplate(String userName, String appointmentDate, String appointmentTime) {

        TemplateComponent bodyComponent = createComponent(ComponentType.BODY, null, null);
        bodyComponent.setParameters(Arrays.asList(
                createParameter(ParameterType.TEXT, userName),
                createParameter(ParameterType.TEXT, appointmentDate),
                createParameter(ParameterType.TEXT, appointmentTime)));

        TemplateComponent rescheduleAppointmentButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 0);
        rescheduleAppointmentButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.RESCHEDULE_APPOINTMENT + appointmentDate + "_T_" + appointmentTime)));

        TemplateComponent cancelAppointmentButtonComponent = createComponent(ComponentType.BUTTON, WhatsAppConstants.QUICK_REPLY, 1);
        cancelAppointmentButtonComponent.setParameters(Collections.singletonList(createParameter(ParameterType.PAYLOAD, WhatsAppConstants.CANCEL_APPOINTMENT + appointmentDate + "_T_" + appointmentTime)));

        return Arrays.asList(bodyComponent, rescheduleAppointmentButtonComponent, cancelAppointmentButtonComponent);
    }

    public String sendTemplateByDeliveryStatus(DeliveryStatus deliveryStatus) {

        return switch (deliveryStatus) {
            case SHIPPED, ORDER_CONFIRMED -> WhatsAppConstants.ORDER_STATUS_SHIPPED_TEMPLATE;
            case DELIVERED -> WhatsAppConstants.ORDER_STATUS_DELIVERED_SUCCESS_TEMPLATE;
            case OUT_FOR_DELIVERY -> WhatsAppConstants.ORDER_STATUS_OUT_FOR_DELIVERED_TEMPLATE;
            case CANCELLED -> WhatsAppConstants.ORDER_STATUS_CANCELLED_TEMPLATE;
            default -> null;
        };
    }
}