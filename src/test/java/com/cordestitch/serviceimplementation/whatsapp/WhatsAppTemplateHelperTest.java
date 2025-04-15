package com.cordestitch.serviceimplementation.whatsapp;

import com.cordestitch.enums.ComponentType;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.ParameterType;
import com.cordestitch.request.webhook.TextRequest;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.service.serviceimplementation.whatsapp.WhatsAppTemplateHelper;
import com.cordestitch.util.WhatsAppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhatsAppTemplateHelperTest {

    @InjectMocks
    private WhatsAppTemplateHelper whatsAppTemplateHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderConfirmationTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateOrderConfirmationTemplate();
        List<TemplateComponent> orderConfirmTemplate = whatsAppTemplateHelper.createOrderConfirmationTemplate("Vinay", "Od123", "21-01-2024");
        assertEquals(expectedTemplateComponentList.get(0).getParameters().get(0).getImageParameter(), orderConfirmTemplate.get(0).getParameters().get(0).getImageParameter());
        assertEquals(expectedTemplateComponentList, orderConfirmTemplate);
    }

    @Test
    void createOrderCancellationTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateOrderCancellationTemplate();
        List<TemplateComponent> orderCancelTemplate = whatsAppTemplateHelper.createOrderCancellationTemplate("Vinay", "Od123");
        assertEquals(expectedTemplateComponentList.get(0).getParameters().get(0).getImageParameter(), orderCancelTemplate.get(0).getParameters().get(0).getImageParameter());
        assertEquals(expectedTemplateComponentList, orderCancelTemplate);
    }

    @Test
    void createOrderDeliveredTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateOrderDeliveredTemplate();
        List<TemplateComponent> orderDeliveredTemplate = whatsAppTemplateHelper.createOrderDeliveredTemplate("Vinay", "Od123", "21-01-2024");
        assertEquals(expectedTemplateComponentList.get(0).getParameters().get(0).getImageParameter(), orderDeliveredTemplate.get(0).getParameters().get(0).getImageParameter());
        assertEquals(expectedTemplateComponentList, orderDeliveredTemplate);
    }

    @Test
    void createOtpTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateOtpTemplate();
        List<TemplateComponent> otpTemplate = whatsAppTemplateHelper.createOtpTemplate("987678");
        assertEquals(expectedTemplateComponentList.get(0).getParameters().get(0).getImageParameter(), otpTemplate.get(0).getParameters().get(0).getImageParameter());
        assertEquals(expectedTemplateComponentList, otpTemplate);
    }

    @Test
    void createMessageRequest() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppTemplateHelper.createMessageRequest("12345", "template_name", "english", getComponents());
        assertEquals(expectedWhatsAppMessageRequest, whatsAppMessageRequest);
    }

    @Test
    void createMarkAsReadRequest() {
        MarkAsReadRequest expectedMessageTemplate = getMarkAsReadRequest();
        MarkAsReadRequest messageTemplate = whatsAppTemplateHelper.createMarkAsReadRequest("whatsapp", "read", "12345");
        assertEquals(expectedMessageTemplate, messageTemplate);
    }

    @Test
    void createTextMessageRequest() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getTextMessageRequest();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppTemplateHelper.createTextMessageRequest("9878987898", "hello world");
        assertEquals(expectedWhatsAppMessageRequest, whatsAppMessageRequest);
    }

    @Test
    void createSomeThingWentWrongTemplate() {

        List<TemplateComponent> expectedTemplateComponentList = getSomeThingWentWrongTemplate();
        List<TemplateComponent> someThingWentWrong = whatsAppTemplateHelper.createSomeThingWentWrongTemplate("Some thing went wrong");
        assertEquals(expectedTemplateComponentList.get(0).getParameters().get(0).getImageParameter(), someThingWentWrong.get(0).getParameters().get(0).getImageParameter());
        assertEquals(expectedTemplateComponentList, someThingWentWrong);
    }

    @Test
    void createOrderStatusTemplate() {

        List<TemplateComponent> expectedOrderStatusTemplate = getOrderStatusTemplate();
        List<TemplateComponent> orderStatusTemplate = whatsAppTemplateHelper.createOrderStatusTemplate("Vinay", "OD123", "21-02-2025", DeliveryStatus.SHIPPED);
        assertEquals(expectedOrderStatusTemplate.get(0).getParameters().get(0).getImageParameter(), orderStatusTemplate.get(0).getParameters().get(0).getImageParameter());
        assertEquals(expectedOrderStatusTemplate, orderStatusTemplate);
    }

    @Test
    void createOrderStatusTemplate_WithNullDeliveryDate_withOrderDeliveredStatus() {
        List<TemplateComponent> expectedOrderStatusTemplate = getOrderStatusTemplateWithoutDate();
        List<TemplateComponent> orderStatusTemplate = whatsAppTemplateHelper.createOrderStatusTemplate("Vinay", "OD123", null, DeliveryStatus.DELIVERED);
        assertEquals(expectedOrderStatusTemplate, orderStatusTemplate);
    }

    @Test
    void createOrderStatusTemplate_WithNullDeliveryDate_withOrderShippedStatus() {
        List<TemplateComponent> expectedOrderStatusTemplate = getOrderStatusTemplateWithoutDate();
        List<TemplateComponent> orderStatusTemplate = whatsAppTemplateHelper.createOrderStatusTemplate("Vinay", "OD123", null, DeliveryStatus.SHIPPED);
        assertEquals(expectedOrderStatusTemplate, orderStatusTemplate);
    }

    @Test
    void createOrderStatusTemplate_WithOtherDeliveryStatus() {
        List<TemplateComponent> expectedOrderStatusTemplate = getOrderStatusTemplateWithoutDate();
        List<TemplateComponent> orderStatusTemplate = whatsAppTemplateHelper.createOrderStatusTemplate("Vinay", "OD123", "21-02-2025", DeliveryStatus.DELIVERED);
        assertEquals(expectedOrderStatusTemplate, orderStatusTemplate);
    }

    @Test
    void testSendTemplateByDeliveryStatus() {
        assertEquals(WhatsAppConstants.ORDER_STATUS_SHIPPED_TEMPLATE, whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.SHIPPED));
        assertEquals(WhatsAppConstants.ORDER_STATUS_SHIPPED_TEMPLATE, whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED));
        assertEquals(WhatsAppConstants.ORDER_STATUS_DELIVERED_SUCCESS_TEMPLATE, whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.DELIVERED));
        assertEquals(WhatsAppConstants.ORDER_STATUS_OUT_FOR_DELIVERED_TEMPLATE, whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.OUT_FOR_DELIVERY));
        assertEquals(WhatsAppConstants.ORDER_STATUS_CANCELLED_TEMPLATE, whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.CANCELLED));
        assertNull(whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.RETURNED));
    }

    @Test
    void createComponent_ForFooter() {
        TemplateComponent footerComponent = whatsAppTemplateHelper.createComponent(ComponentType.FOOTER, null, null);
        assertNotNull(footerComponent);
        assertEquals(ComponentType.FOOTER.getType(), footerComponent.getType());
        assertNotNull(footerComponent.getParameters());
        assertTrue(footerComponent.getParameters().isEmpty());
    }

    @Test
    void createAlterationAppointmentTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateAlterationAppointmentTemplate();
        List<TemplateComponent> alterationAppointment = whatsAppTemplateHelper.createAlterationAppointmentTemplate("Vinay", "Od123", "21-01-2024", "10:00AM-11:00AM");
        assertEquals(expectedTemplateComponentList.getFirst().getParameters().getFirst().getImageParameter(), alterationAppointment.getFirst().getParameters().getFirst().getImageParameter());
    }

    @Test
    void createAppointmentCancellationTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateAlterationAppointmentTemplate();
        List<TemplateComponent> alterationCancellation = whatsAppTemplateHelper.createAppointmentCancellationTemplate("Vinay", "21-01-2024", "10:00AM-11:00AM");
        assertEquals(expectedTemplateComponentList.getFirst().getParameters().getFirst().getImageParameter(), alterationCancellation.getFirst().getParameters().getFirst().getImageParameter());
    }

    @Test
    void createAppointmentRescheduleTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateAlterationAppointmentTemplate();
        List<TemplateComponent> alterationReschedule = whatsAppTemplateHelper.createAppointmentRescheduleTemplate("Vinay", "21-01-2024", "21-01-2000", "10:00AM-11:00AM", "22-01-2000", "10:00AM-11:00AM");
        assertEquals(expectedTemplateComponentList.getFirst().getParameters().getFirst().getImageParameter(), alterationReschedule.getFirst().getParameters().getFirst().getImageParameter());
    }

    @Test
    void createFitAppointmentTemplate() {
        List<TemplateComponent> expectedTemplateComponentList = getCreateAlterationAppointmentTemplate();
        List<TemplateComponent> fitAppointmentTemplate = whatsAppTemplateHelper.createFitAppointmentTemplate("Vinay", "21-01-2024", "21-01-2000");
        assertEquals(expectedTemplateComponentList.getFirst().getParameters().getFirst().getImageParameter(), fitAppointmentTemplate.getFirst().getParameters().getFirst().getImageParameter());
    }

    private List<TemplateComponent> getOrderStatusTemplateWithoutDate() {
        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getBodyTemplateComponent(getTextParameterWithoutDate()));
        return templateComponentList;
    }

    private List<Parameter> getTextParameterWithoutDate() {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(getTextParameter("Vinay"));
        parameterList.add(getTextParameter("OD123"));
        return parameterList;
    }

    private List<TemplateComponent> getOrderStatusTemplate() {

        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getBodyTemplateComponent(getTextParameterForOrderStatus()));
        return templateComponentList;
    }

    private List<Parameter> getTextParameterForOrderStatus() {

        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(getTextParameter("Vinay"));
        parameterList.add(getTextParameter("OD123"));
        parameterList.add(getDateTimeParameter("21-02-2025"));
        return parameterList;
    }

    private List<TemplateComponent> getSomeThingWentWrongTemplate() {

        List<TemplateComponent> templateComponentList = new ArrayList<>();
        List<Parameter> parameters = Collections.singletonList(getTextParameter("Some thing went wrong"));
        templateComponentList.add(getBodyTemplateComponent(parameters));
        return templateComponentList;
    }

    private WhatsAppMessageRequest getTextMessageRequest() {

        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct(WhatsAppConstants.WHATSAPP);
        whatsAppMessageRequest.setTo("9878987898");
        whatsAppMessageRequest.setType(WhatsAppConstants.TEXT);

        TextRequest textRequest = new TextRequest();
        textRequest.setBody("hello world");
        whatsAppMessageRequest.setText(textRequest);
        return whatsAppMessageRequest;
    }

    private MarkAsReadRequest getMarkAsReadRequest() {
        MarkAsReadRequest markAsReadRequest = new MarkAsReadRequest();
        markAsReadRequest.setMessageId("12345");
        markAsReadRequest.setStatus("read");
        markAsReadRequest.setMessagingProduct("whatsapp");
        return markAsReadRequest;
    }

    private WhatsAppMessageRequest getWhatsAppMessageRequest() {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct("whatsapp");
        whatsAppMessageRequest.setTo("12345");
        whatsAppMessageRequest.setType("template");
        whatsAppMessageRequest.setText(null);
        whatsAppMessageRequest.setTemplate(getTemplate());
        return whatsAppMessageRequest;
    }

    private MessageTemplate getTemplate() {
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setName("template_name");
        messageTemplate.setLanguage(getLanguage());
        messageTemplate.setComponents(getComponents());
        return messageTemplate;
    }

    private List<TemplateComponent> getComponents() {
        List<TemplateComponent> templateComponentList = new ArrayList<>();
        TemplateComponent templateComponent = new TemplateComponent();
        templateComponent.setType("button");
        templateComponent.setSubType("text");
        templateComponent.setIndex(0);
        templateComponent.setParameters(getParametersList());
        templateComponentList.add(templateComponent);
        return templateComponentList;

    }

    private List<Parameter> getParametersList() {
        List<Parameter> parameterList = new ArrayList<>();
        Parameter parameter = new Parameter();
        parameter.setType("text");
        parameter.setText("some random text");
        parameter.setPayload("payload data");
        parameter.setDateTimeParameter(getDateTimeParameter());
        parameter.setImageParameter(getImageParameter());
        parameterList.add(parameter);
        return parameterList;
    }

    private DateTimeParameter getDateTimeParameter() {
        DateTimeParameter dateTimeParameter = new DateTimeParameter();
        dateTimeParameter.setFallbackValue("21 JAN 2025");
        return dateTimeParameter;
    }

    private TemplateLanguage getLanguage() {
        TemplateLanguage templateLanguage = new TemplateLanguage();
        templateLanguage.setPolicy("deterministic");
        templateLanguage.setCode("english");
        return templateLanguage;
    }

    private List<TemplateComponent> getCreateOtpTemplate() {

        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getButtonComponentForText());
        templateComponentList.add(getBodyTemplateComponent(getTextParameterForOtp()));
        return templateComponentList;
    }

    private TemplateComponent getButtonComponentForText() {
        TemplateComponent component = new TemplateComponent();
        component.setType(ComponentType.BUTTON.getType());
        component.setSubType(WhatsAppConstants.SUBSTRING_URL);
        component.setIndex(0);
        component.setParameters(getTextParameterForOtp());
        return component;

    }

    private List<Parameter> getTextParameterForOtp() {

        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(getTextParameter("987678"));
        return parameterList;
    }

    private List<TemplateComponent> getCreateOrderDeliveredTemplate() {

        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getHeaderTemplateComponent(getImageListParameter()));
        templateComponentList.add(getBodyTemplateComponent(getTextAndDateTimeParameter()));
        return templateComponentList;
    }

    private List<TemplateComponent> getCreateOrderCancellationTemplate() {

        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getHeaderTemplateComponent(getImageListParameter()));
        templateComponentList.add(getBodyTemplateComponent(getTextParameterForCancelOrder()));
        return templateComponentList;
    }

    private List<Parameter> getTextParameterForCancelOrder() {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(getTextParameter("Vinay"));
        parameterList.add(getTextParameter("Od123"));
        return parameterList;
    }

    private List<TemplateComponent> getCreateOrderConfirmationTemplate() {
        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getHeaderTemplateComponent(getImageListParameter()));
        templateComponentList.add(getBodyTemplateComponent(getTextAndDateTimeParameter()));
        templateComponentList.add(getButtonComponent(0, WhatsAppConstants.TRACK_ORDER + "Od123"));
        templateComponentList.add(getButtonComponent(1, WhatsAppConstants.CANCEL_ORDER + "Od123"));
        return templateComponentList;
    }

    private List<TemplateComponent> getCreateAlterationAppointmentTemplate() {
        List<TemplateComponent> templateComponentList = new ArrayList<>();
        templateComponentList.add(getBodyTemplateComponent(Collections.singletonList(getTextParameter("Vinay"))));
        templateComponentList.add(getButtonComponent(0, WhatsAppConstants.RESCHEDULE_APPOINTMENT));
        templateComponentList.add(getButtonComponent(1, WhatsAppConstants.CANCEL_APPOINTMENT));
        return templateComponentList;
    }

    private TemplateComponent getButtonComponent(Integer index, String value) {
        TemplateComponent component = new TemplateComponent();
        component.setType(ComponentType.BUTTON.getType());
        component.setSubType(WhatsAppConstants.QUICK_REPLY);
        component.setIndex(index);
        component.setParameters(getButtonParameter(value));
        return component;
    }

    private List<Parameter> getButtonParameter(String value) {
        List<Parameter> parameterList = new ArrayList<>();
        Parameter parameter = new Parameter();
        parameter.setType(ParameterType.PAYLOAD.getType());
        parameter.setPayload(value);
        parameterList.add(parameter);
        return parameterList;
    }

    private TemplateComponent getBodyTemplateComponent(List<Parameter> parameterList) {
        TemplateComponent component = new TemplateComponent();
        component.setType(ComponentType.BODY.getType());
        component.setParameters(parameterList);
        return component;
    }

    private TemplateComponent getHeaderTemplateComponent(List<Parameter> parameterList) {
        TemplateComponent component = new TemplateComponent();
        component.setType(ComponentType.HEADER.getType());
        component.setParameters(parameterList);
        return component;
    }

    private List<Parameter> getTextAndDateTimeParameter() {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(getTextParameter("Vinay"));
        parameterList.add(getTextParameter("Od123"));
        parameterList.add(getDateTimeParameter("21-01-2024"));
        return parameterList;
    }

    private Parameter getDateTimeParameter(String value) {
        Parameter parameter = new Parameter();
        parameter.setType(ParameterType.DATE_TIME.getType());
        DateTimeParameter dateTimeParameter = new DateTimeParameter();
        dateTimeParameter.setFallbackValue(value);
        parameter.setDateTimeParameter(dateTimeParameter);
        return parameter;
    }

    private Parameter getTextParameter(String value) {
        Parameter nameParameter = new Parameter();
        nameParameter.setType(ParameterType.TEXT.getType());
        nameParameter.setText(value);
        return nameParameter;
    }

    private List<Parameter> getImageListParameter() {
        List<Parameter> parameterList = new ArrayList<>();
        Parameter parameter = new Parameter();
        parameter.setType(ParameterType.IMAGE.getType());
        ImageParameter imageParameter = new ImageParameter();
        imageParameter.setLink(WhatsAppConstants.IMAGE_URL);
        parameter.setImageParameter(imageParameter);
        parameterList.add(parameter);
        return parameterList;
    }

    private ImageParameter getImageParameter() {
        ImageParameter imageParameter = new ImageParameter();
        imageParameter.setLink("https://image.com");
        return imageParameter;
    }
}