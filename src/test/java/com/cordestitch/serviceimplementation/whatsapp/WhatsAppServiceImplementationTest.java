package com.cordestitch.serviceimplementation.whatsapp;

import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.request.webhook.TextRequest;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.serviceimplementation.whatsapp.WhatsAppInteractiveHelper;
import com.cordestitch.service.serviceimplementation.whatsapp.WhatsAppServiceImplementation;
import com.cordestitch.service.serviceimplementation.whatsapp.WhatsAppTemplateHelper;
import com.cordestitch.util.WhatsAppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WhatsAppServiceImplementationTest {

    @InjectMocks
    private WhatsAppServiceImplementation whatsAppServiceImplementation;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private WhatsAppTemplateHelper whatsAppTemplateHelper;
    @Mock
    private WhatsAppInteractiveHelper whatsAppInteractiveHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(whatsAppServiceImplementation, "baseApiUrl", "http://abc.com");
        ReflectionTestUtils.setField(whatsAppServiceImplementation, "accessToken", "jhsdkasklnka");
        ReflectionTestUtils.setField(whatsAppServiceImplementation, "whatsappPhoneId", "98918092620280");
    }

    @Test
    void notifyOrderConfirmation() {
        when(whatsAppTemplateHelper.createMessageRequest(any(), anyString(), any(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.notifyOrderConfirmation("95876545499", "Vinay", "OD78197297", "21-08-2024");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void notifyOrderCancellation() {
        when(whatsAppTemplateHelper.createMessageRequest(any(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.notifyOrderCancellation("9876564323", "Vinay", "Od87656");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void notifyOrderDelivered() {
        when(whatsAppTemplateHelper.createMessageRequest(any(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.notifyOrderDelivered("9876564323", "Vinay", "Od87656", "21-08-2024");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void generateOtp() {
        when(whatsAppTemplateHelper.createMessageRequest(any(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.generateOtp("9876564323", "987656");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void markAsRead() {
        when(whatsAppTemplateHelper.createMarkAsReadRequest(any(), anyString(), anyString())).thenReturn(getMarkAsReadRequest());
        SuccessResponse response = whatsAppServiceImplementation.markAsRead("whatsapp", "read", "yikes");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendCancellationReasons() {
        when(whatsAppInteractiveHelper.getCancellationReasonSections(anyString())).thenReturn(getInteractiveSections());
        when(whatsAppInteractiveHelper.getInteractiveMessageRequest(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendCancellationReasons("9876566543", "OD876567");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void cancelOrderActionRequest() {
        when(whatsAppInteractiveHelper.getCancellationButtons(anyString(), anyString(), anyString())).thenReturn(getInteractiveButtons());
        when(whatsAppInteractiveHelper.getInteractiveMessageRequest(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.cancelOrderActionRequest("9876566543", "OD876567");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void notifyOrderCancelledFromWhatsapp() {
        when(whatsAppTemplateHelper.createMessageRequest(anyString(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.notifyOrderCancelledFromWhatsapp("9878876776");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendSomeThingWentWrong() {
        when(whatsAppTemplateHelper.createMessageRequest(anyString(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendSomeThingWentWrong("8756777897", "went wrong");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());

    }

    @Test
    void sendWelcomeMessage() {
        when(whatsAppInteractiveHelper.createWelcomeMessageButtons()).thenReturn(getInteractiveButtons());
        SuccessResponse response = whatsAppServiceImplementation.sendWelcomeMessage("8787978977");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendTextMessage() {
        when(whatsAppTemplateHelper.createTextMessageRequest(anyString(), anyString())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendTextMessage("989876876", "message body");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendCatalogAndSupport() {
        when(whatsAppInteractiveHelper.getCatalogAndSupportSections()).thenReturn(getInteractiveButtons());
        when(whatsAppInteractiveHelper.getInteractiveMessageRequest(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendCatalogAndSupport("9898989898");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendTopCategoryInteractiveList() {
        when(whatsAppInteractiveHelper.createTopCategorySections()).thenReturn(getInteractiveSections());
        when(whatsAppInteractiveHelper.getInteractiveMessageRequest(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendTopCategoryInteractiveList("988988899");
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendOrderStatusMessageWhenStatusIsCancelled() {
        when(whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.CANCELLED)).thenReturn((WhatsAppConstants.ORDER_STATUS_CANCELLED_TEMPLATE));
        when(whatsAppTemplateHelper.createMessageRequest(anyString(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendOrderStatusMessage("989876578", "Vinay", "Od565", "21-09-2024", DeliveryStatus.CANCELLED);
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendOrderStatusMessageWhenStatusIsShipped() {
        when(whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.SHIPPED)).thenReturn((WhatsAppConstants.ORDER_STATUS_SHIPPED_TEMPLATE));
        when(whatsAppTemplateHelper.createMessageRequest(anyString(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendOrderStatusMessage("989876578", "Vinay", "Od565", "21-09-2024", DeliveryStatus.SHIPPED);
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendOrderStatusMessageWhenStatusIsDelivered() {
        when(whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.DELIVERED)).thenReturn((WhatsAppConstants.ORDER_STATUS_DELIVERED_SUCCESS_TEMPLATE));
        when(whatsAppTemplateHelper.createMessageRequest(anyString(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendOrderStatusMessage("989876578", "Vinay", "Od565", "21-09-2024", DeliveryStatus.DELIVERED);
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendOrderStatusMessageWhenStatusIsOutForDelivery() {
        when(whatsAppTemplateHelper.sendTemplateByDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED)).thenReturn((WhatsAppConstants.ORDER_STATUS_OUT_FOR_DELIVERED_TEMPLATE));
        when(whatsAppTemplateHelper.createMessageRequest(anyString(), anyString(), anyString(), any())).thenReturn(getWhatsAppMessageRequest());
        SuccessResponse response = whatsAppServiceImplementation.sendOrderStatusMessage("989876578", "Vinay", "Od565", "21-09-2024", DeliveryStatus.OUT_FOR_DELIVERY);
        assertEquals(WhatsAppConstants.MESSAGE_SENT, response.getMessage());
    }

    @Test
    void sendHttpPostRequest() {

        String baseUrl = "http://abc.com/sendMessage";
        Object requestBody = new Object();
        Class<String> responseType = String.class;
        String mockResponse = "Success";

        when(restTemplate.postForObject(eq(baseUrl), any(), eq(responseType))).thenReturn(mockResponse);

        whatsAppServiceImplementation.sendHttpPostRequest(baseUrl, requestBody, responseType);

        verify(restTemplate, times(1)).postForObject(eq(baseUrl), any(), eq(responseType));
    }

    @Test
    void sendHttpPostRequest_whenBaseUrlIsNull() {

        Object requestBody = new Object();
        Class<String> responseType = String.class;

        doThrow(new RuntimeException("Simulated HTTP error"))
                .when(restTemplate).postForObject(eq("baseUrl"), any(HttpEntity.class), eq(responseType));

        assertDoesNotThrow(() -> whatsAppServiceImplementation.sendHttpPostRequest("baseUrl", requestBody, responseType),
                "Exception should be caught inside the method.");
    }

    private MarkAsReadRequest getMarkAsReadRequest() {
        MarkAsReadRequest markAsReadRequest = new MarkAsReadRequest();
        markAsReadRequest.setMessagingProduct("whatsapp");
        markAsReadRequest.setStatus("read");
        markAsReadRequest.setMessageId("id");
        return markAsReadRequest;
    }

    private WhatsAppMessageRequest getWhatsAppMessageRequest() {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct("Whatsapp");
        whatsAppMessageRequest.setTo("12345");
        whatsAppMessageRequest.setType("Template");
        whatsAppMessageRequest.setText(getTextRequest());
        whatsAppMessageRequest.setTemplate(getTemplate());
        whatsAppMessageRequest.setInteractive(getInteractiveMessage());
        return whatsAppMessageRequest;
    }

    private InteractiveRequest getInteractiveMessage() {
        InteractiveRequest interactiveRequest = new InteractiveRequest();
        interactiveRequest.setType("button");
        interactiveRequest.setHeader(getInteractiveHeader());
        interactiveRequest.setBody(getInteractiveBody());
        interactiveRequest.setAction(getInteractiveAction());
        return interactiveRequest;
    }

    private ActionRequest getInteractiveAction() {
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setButton("button name");
        actionRequest.setSections(getInteractiveSections());
        actionRequest.setButtons(getInteractiveButtons());
        return actionRequest;
    }

    private List<InteractiveButtonRequest> getInteractiveButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        InteractiveButtonRequest buttonRequest = new InteractiveButtonRequest();
        buttonRequest.setType("button type");
        buttonRequest.setReply(getInterActiveButtonReplyRequest());
        interactiveButtonRequestList.add(buttonRequest);
        return interactiveButtonRequestList;
    }

    private ReplyRequest getInterActiveButtonReplyRequest() {
        ReplyRequest replyRequest = new ReplyRequest();
        replyRequest.setId("reply_id");
        replyRequest.setTitle("reply_Tittle");
        return replyRequest;
    }

    private List<SectionRequest> getInteractiveSections() {
        List<SectionRequest> sectionRequestList = new ArrayList<>();
        SectionRequest sectionRequest = new SectionRequest();
        sectionRequest.setTitle("section tittle");
        sectionRequest.setRows(getSectionRequestRows());
        sectionRequestList.add(sectionRequest);
        return sectionRequestList;
    }

    private List<RowRequest> getSectionRequestRows() {
        List<RowRequest> rowRequestList = new ArrayList<>();
        RowRequest rowRequest = new RowRequest();
        rowRequest.setId("row id");
        rowRequest.setTitle("row tittle");
        rowRequest.setDescription("row description");
        rowRequestList.add(rowRequest);
        return rowRequestList;
    }

    private BodyRequest getInteractiveBody() {
        BodyRequest bodyRequest = new BodyRequest();
        bodyRequest.setText("body text");
        return bodyRequest;
    }

    private HeaderRequest getInteractiveHeader() {
        HeaderRequest headerRequest = new HeaderRequest();
        headerRequest.setType("text");
        headerRequest.setText("some random text");
        return headerRequest;
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

    private ImageParameter getImageParameter() {
        ImageParameter imageParameter = new ImageParameter();
        imageParameter.setLink("https://image.com");
        return imageParameter;
    }

    private DateTimeParameter getDateTimeParameter() {
        DateTimeParameter dateTimeParameter = new DateTimeParameter();
        dateTimeParameter.setFallbackValue("21 JAN 2025");
        return dateTimeParameter;
    }


    private TemplateLanguage getLanguage() {
        TemplateLanguage templateLanguage = new TemplateLanguage();
        templateLanguage.setPolicy("hello");
        templateLanguage.setCode("english");
        return templateLanguage;
    }

    private TextRequest getTextRequest() {
        TextRequest textRequest = new TextRequest();
        textRequest.setBody("GJSj");
        return textRequest;
    }
}