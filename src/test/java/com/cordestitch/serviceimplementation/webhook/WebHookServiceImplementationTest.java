package com.cordestitch.serviceimplementation.webhook;

import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.enums.ReturnStatus;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.webhook.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.order.CancelOrderResponse;
import com.cordestitch.service.service.order.OrderService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.service.serviceimplementation.webhook.WebHookServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.WhatsAppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WebHookServiceImplementationTest {

    @InjectMocks
    private WebHookServiceImplementation webHookServiceImplementation;
    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(webHookServiceImplementation, "accessToken", "12345");
    }

    @Test
    void testVerifyWebhook() {
        String token = "12345";
        String challenge = "validChallenge";
        String result = webHookServiceImplementation.verifyWebhook(WhatsAppConstants.SUBSCRIBE, token, challenge);
        assertEquals(challenge, result, "The challenge should be returned when the token is valid");
    }

    @Test
    void testVerifyWebhook_invalidAccessToken() {
        String mode = "incorrectMode";
        String challenge = "validChallenge";
        String result = webHookServiceImplementation.verifyWebhook(mode, "1234", challenge);
        assertNull(result, "The result should be null when the token is null");
    }

    @Test
    void testVerifyWebhook_invalidMode() {
        String token = "12345";
        String challenge = "validChallenge";
        String result = webHookServiceImplementation.verifyWebhook("hello", token, challenge);
        assertNull(result, "The result should be null when the mode is null");
    }

    @Test
    void testVerifyWebhook_when_modeTokenChallenge_isNull() {
        String challenge = "validChallenge";
        String result = webHookServiceImplementation.verifyWebhook("hello", "12348", challenge);
        assertNull(result, "The result should be null when the mode is null");
    }

    @ParameterizedTest
    @ValueSource(ints = {
            0,
            7
    })
    void testHandleWebhookEvent_when_interactive_listReply_for_cancellation(int returnDays) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setButtonReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle(WhatsAppConstants.FOUND_BETTER_PRICE);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId("IT9876878");

        OrderItemEntity orderItemEntity = getOrderItemEntity();
        orderItemEntity.setReturnDaysPolicy(returnDays);
        when(orderItemRepository.findByOrderItemId(anyString())).thenReturn(orderItemEntity);
        when(orderService.cancelOrder(any())).thenReturn(getCancelOrderResponse());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }


    @Test
    void testHandleWebhookEvent_when_interactive_listReply_for_cancellation_whenOrderStatus_isNotCancelled() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setButtonReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle(WhatsAppConstants.FOUND_BETTER_PRICE);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId("IT9876878");

        when(orderItemRepository.findByOrderItemId(anyString())).thenReturn(getOrderItemEntity());
        CancelOrderResponse cancelOrderResponse = getCancelOrderResponse();
        cancelOrderResponse.setOrderStatus(OrderStatus.CONFIRMED);

        when(orderService.cancelOrder(any())).thenReturn(cancelOrderResponse);
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_interactive_listReply_and_buttonReply_isNull() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setButtonReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setListReply(null);

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }


    @Test
    void testHandleWebhookEvent_when_entryRequest_isNull() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.setEntry(null);

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_changeRequest_isNull() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("whatsapp");

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_messageRequest_isNull() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().setMessages(null);

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_messageRequest_isEmpty() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().setMessages(Collections.emptyList());

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_interactive_listReply_for_cancellation_failure() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setButtonReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle(WhatsAppConstants.FOUND_BETTER_PRICE);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId("IT9876878");

        OrderItemEntity orderItemEntity = getOrderItemEntity();
        orderItemEntity.setOrderItemId(null);
        when(orderItemRepository.findByOrderItemId(anyString())).thenReturn(null);
        when(orderService.cancelOrder(any())).thenReturn(getCancelOrderResponse());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_interactive_listReply_toSend_availableSlotDates() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle("2025-02-21");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId("IT9876878");

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "RESCHEDULE_IT9876765_2025-03-21_T_10:00-11:00_N_2025-03-22_NT_10:00-11:00",
            "RESCHEDULE_t_2025-03-21_T_10:00-11:00_N_2025-03-22_NT_10:00-11:00",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N_2025-03-22_NT_10:00-11:00",
            "RESCHEDULE_t _2025-03-21_T_10:00gh-11:00hh_N_2025-03-22_NT_10:00gb-11:00pj",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N__NT_10:00-11:00",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N_2025-03-22_NT_-11:00",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N_2025-03-22_NT_10:00-",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N__NT_-11:00",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N_2025-03-22_NT_",
            "RESCHEDULE_t _2025-03-21_T_10:00-11:00_N__NT_10:00-"
    })
    void testHandleWebhookEvent_when_interactive_listReply_toRescheduleTimeSlot(String listReplyId) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle("10:00AM-11:00AM");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId(listReplyId);

        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(getUserEntity());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_interactive_listReply_when_idAndTittle_IsNotPresent() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId("id");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle("Jeans pants");

        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(getUserEntity());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_interactive_listReply_when_idAndTittle_IsPresent() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle("Formal pants");
        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(getUserEntity());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            WhatsAppConstants.YES_BUTTON,
            WhatsAppConstants.NO_BUTTON,
            WhatsAppConstants.DO_NOT_CANCEL_BUTTON,
            WhatsAppConstants.START_BUTTON,
            WhatsAppConstants.STOP_BUTTON,
            WhatsAppConstants.TOP_CATEGORY,
            WhatsAppConstants.TRENDING_PRODUCT,
            WhatsAppConstants.CONTACT_US,
            "hello World"
    })
    void testHandleWebhookEvent_with_various_interactive_buttonReplies(String buttonReply) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setListReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getButtonReply().setTitle(buttonReply);
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CANCEL_IT9876765_2025-02-21_T_10:00-11:00",
            "CANCEL_t_2025-02-21_T_10:00-11:00",
            "CANCEL_t _2025-02-21_T_10:00-11:00"
    })
    void testHandleWebhookEvent_with_interactive_buttonReply_for_yesCancelButton(String buttonReplyId) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");

        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setListReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getButtonReply().setTitle(WhatsAppConstants.YES_CANCEL_BUTTON);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getButtonReply().setId(buttonReplyId);
        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(getUserEntity());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "878787878787",
            "9187878787",
            "987878787",
            "919876543210"
    })
    void testHandleWebhookEvent_with_interactive_buttonReply_for_yesCancelButton_whenPhoneNumber_isDifferent(String recipientNumber) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");

        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().setFrom(recipientNumber);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setListReply(null);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getButtonReply().setTitle(WhatsAppConstants.YES_CANCEL_BUTTON);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getButtonReply().setId("CANCEL_IT9876765_2025-02-21_T_10:00-11:00");
        when(userRepository.findUserByPhoneNumber(anyString())).thenReturn(getUserEntity());
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_messageType_is_textMessage() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().setType(WhatsAppConstants.TEXT);

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            WhatsAppConstants.CANCEL_ORDER_BUTTON,
            WhatsAppConstants.CANCEL_APPOINTMENT_BUTTON,
            WhatsAppConstants.RESCHEDULE_APPOINTMENT_BUTTON,
            "hello World"
    })
    void testHandleWebhookEvent_when_messageType_is_buttonMessages(String buttonType) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().setType(WhatsAppConstants.BUTTON);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getButton().setText(buttonType);

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "TRACK_IT7676767",
            "TRACK_OD545454"
    })
    void testHandleWebhookEvent_when_messageType_is_buttonMessage_for_trackingOrder(String payLoad) {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().setType(WhatsAppConstants.BUTTON);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getButton().setText(WhatsAppConstants.TRACK_ORDER_BUTTON);
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getButton().setPayload(payLoad);
        when(orderItemRepository.findByOrderItemId(anyString())).thenReturn(getOrderItemEntity());

        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_messageType_is_unknown() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().setType("hello");
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_when_messageId_isNull() {
        WebHookEventRequest request = getWebHookRequest();
        request.getEntry().getFirst().getChanges().getFirst().setField("messages");
        request.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().setId(null);
        SuccessResponse response = webHookServiceImplementation.handleWebhookEvent(request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.SUCCESS, response.getMessage());
    }

    @Test
    void testHandleWebhookEvent_whenCancellationThreadInterrupted() throws Exception {

        WebHookEventRequest webhookEvent = getWebHookRequest();
        webhookEvent.getEntry().getFirst().getChanges().getFirst().setField("messages");
        webhookEvent.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().setButtonReply(null);
        webhookEvent.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setTitle(WhatsAppConstants.FOUND_BETTER_PRICE);
        webhookEvent.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getInteractive().getListReply().setId("IT9876878");

        when(orderService.cancelOrder(any())).thenReturn(getCancelOrderResponse());
        when(orderItemRepository.findByOrderItemId(anyString())).thenReturn(getOrderItemEntity());

        Thread testThread = new Thread(() -> webHookServiceImplementation.handleWebhookEvent(webhookEvent));
        testThread.start();

        Thread.sleep(10);

        testThread.interrupt();
        testThread.join();

        verify(whatsAppService).markAsRead("whatsapp", "read", "12345678");
        verify(whatsAppService).notifyOrderCancelledFromWhatsapp("8787878787");
    }


    private CancelOrderResponse getCancelOrderResponse() {
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        cancelOrderResponse.setCancelledDate(LocalDateTime.now());
        cancelOrderResponse.setOrderDate(LocalDateTime.now().minusDays(3));
        cancelOrderResponse.setOrderId("IT9876878");
        cancelOrderResponse.setOrderStatus(OrderStatus.CANCELED);
        cancelOrderResponse.setDeliveryStatus(DeliveryStatus.CANCELLED);
        cancelOrderResponse.setMessage("Cancelled");
        cancelOrderResponse.setRefundMessage("Refund message");
        return cancelOrderResponse;
    }

    private WebHookEventRequest getWebHookRequest() {
        WebHookEventRequest request = new WebHookEventRequest();
        request.setObject("whatsapp");
        request.setEntry(getEntryRequest());
        return request;
    }

    private List<EntryRequest> getEntryRequest() {
        List<EntryRequest> entryRequests = new ArrayList<>();
        EntryRequest request = new EntryRequest();
        request.setId("123");
        request.setChanges(getChangesRequest());
        entryRequests.add(request);
        return entryRequests;
    }

    private List<ChangeRequest> getChangesRequest() {
        List<ChangeRequest> changeRequests = new ArrayList<>();
        ChangeRequest request = new ChangeRequest();
        request.setField("value");
        request.setValue(getValueRequest());
        changeRequests.add(request);
        return changeRequests;
    }

    private ValueRequest getValueRequest() {
        ValueRequest request = new ValueRequest();
        request.setMessagingProduct("whatsapp");
        request.setMetadata(getMetaData());
        request.setStatuses(getStatues());
        request.setMessages(getMessages());
        request.setContacts(getContacts());
        return request;
    }

    private List<ContactRequest> getContacts() {
        List<ContactRequest> contactRequests = new ArrayList<>();
        ContactRequest request = new ContactRequest();
        request.setProfile(new ProfileRequest());
        request.setWaId("id");
        contactRequests.add(request);
        return contactRequests;
    }

    private List<MessageRequest> getMessages() {
        List<MessageRequest> messageRequests = new ArrayList<>();
        MessageRequest request = new MessageRequest();
        request.setContext(new ContextRequest());
        request.setFrom("8787878787");
        request.setId("12345678");
        request.setType("messageType");
        request.setTimestamp("timestamp");
        request.setType("interactive");
        request.setText(getTextRequest());
        request.setButton(getButtonRequest());
        request.setInteractive(getInterActiveRequest());
        messageRequests.add(request);
        return messageRequests;
    }

    private InterActiveRequest getInterActiveRequest() {
        InterActiveRequest interActiveRequest = new InterActiveRequest();
        interActiveRequest.setType("interactive");
        interActiveRequest.setListReply(getListReply());
        interActiveRequest.setButtonReply(getListReply());
        return interActiveRequest;
    }

    private ListReplyRequest getListReply() {
        ListReplyRequest listReplyRequest = new ListReplyRequest();
        listReplyRequest.setId("id");
        listReplyRequest.setTitle("tittle");
        listReplyRequest.setDescription("description");
        return listReplyRequest;
    }

    private ButtonRequest getButtonRequest() {
        ButtonRequest buttonRequest = new ButtonRequest();
        buttonRequest.setPayload("payload");
        buttonRequest.setText("text");
        return buttonRequest;
    }

    private TextRequest getTextRequest() {
        TextRequest textRequest = new TextRequest();
        textRequest.setBody("text");
        return textRequest;
    }

    private List<StatusRequest> getStatues() {
        List<StatusRequest> statusRequests = new ArrayList<>();
        StatusRequest request = new StatusRequest();
        request.setId("id");
        request.setStatus("read");
        request.setTimestamp("time-stamp");
        request.setRecipientId("recipient_id");
        request.setConversation(new Conversation());
        request.setPricing(new Pricing());
        statusRequests.add(request);
        return statusRequests;
    }

    private MetadataRequest getMetaData() {
        MetadataRequest request = new MetadataRequest();
        request.setDisplayPhoneNumber("1234567890");
        request.setPhoneNumberId("9876543210");
        return request;
    }

    private OrderItemEntity getOrderItemEntity() {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(getOrderEntity());
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setRedeemedLoyaltyPoints(5.0);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(154.0);
        orderItemEntity.setProductOfferPercentage(20.0);
        orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderItemEntity.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        return orderItemEntity;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId("123");
        orderEntity.setUserEntity(new UserEntity());
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setPaymentEntity(new PaymentEntity());
        orderEntity.setOrderStatus(OrderStatus.DELIVERED);
        orderEntity.setPaymentMethod(Constants.RAZORPAY);
        orderEntity.setAddressEntity(new AddressEntity());
        orderEntity.setTotalAmount(150.0);
        List<OrderItemEntity> orderItemEntities = List.of(new OrderItemEntity());
        orderEntity.setOrderItemEntities(orderItemEntities);
        return orderEntity;
    }

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("1");
        userEntity.setOrderEntities(new ArrayList<>());
        userEntity.setUserCreatedAt(LocalDateTime.now());
        userEntity.setReferredReferralCode("123456");
        userEntity.setReferralCode("123456");
        userEntity.setUserType("customer");
        userEntity.setAuthenticationSource("google");
        userEntity.setEmailAddressVerified(true);
        userEntity.setFirstName("jay");
        userEntity.setEmailAddress("jay@gmail.com");
        userEntity.setLastName("doe");
        userEntity.setPhoneNumber("1234567890");
        userEntity.setPhoneNumberVerified(true);
        userEntity.setAddressEntityList(List.of(new AddressEntity()));
        userEntity.setCardEntities(List.of(new CardEntity()));
        return userEntity;
    }
}