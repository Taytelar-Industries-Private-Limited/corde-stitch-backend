package com.cordestitch.serviceimplementation.whatsapp;

import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.enums.ReturnStatus;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.whatsapp.*;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.BookedSlotTimes;
import com.cordestitch.response.alteration.SlotAvailability;
import com.cordestitch.response.alteration.SlotTimes;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.service.serviceimplementation.alteration.AlterationServiceHelper;
import com.cordestitch.service.serviceimplementation.whatsapp.WhatsAppInteractiveHelper;
import com.cordestitch.util.WhatsAppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WhatsAppInteractiveHelperTest {


    @InjectMocks
    @Spy
    private WhatsAppInteractiveHelper whatsAppInteractiveHelper;

    @Mock
    private AlterationServiceHelper alterationServiceHelper;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInteractiveMessageRequest_when_interactive_type_is_list() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        List<SectionRequest> expectedSections = getInterActiveSections();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest("9148671766", "header text", "body Text", "button Text", "list", getInterActiveSections(), getInterActiveButtons());
        assertEquals(expectedWhatsAppMessageRequest.getMessagingProduct(), whatsAppMessageRequest.getMessagingProduct());
        assertEquals(expectedSections, whatsAppMessageRequest.getInteractive().getAction().getSections());
    }

    @Test
    void createInteractiveMessageRequest_when_interactive_type_is_Button() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        List<InteractiveButtonRequest> expectedInteractiveButtonRequests = getInterActiveButtons();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest("9148671766", "header text", "body Text", "button Text", "button", getInterActiveSections(), getInterActiveButtons());
        assertEquals(expectedWhatsAppMessageRequest.getMessagingProduct(), whatsAppMessageRequest.getMessagingProduct());
        assertEquals(expectedInteractiveButtonRequests, whatsAppMessageRequest.getInteractive().getAction().getButtons());
    }

    @Test
    void createInteractiveMessageRequest_when_interactive_type_is_not_Button() {
        WhatsAppMessageRequest expectedWhatsAppMessageRequest = getWhatsAppMessageRequest();
        WhatsAppMessageRequest whatsAppMessageRequest = whatsAppInteractiveHelper.getInteractiveMessageRequest("9148671766", "header text", "body Text", "button Text", "reply", getInterActiveSections(), getInterActiveButtons());
        assertEquals(expectedWhatsAppMessageRequest.getMessagingProduct(), whatsAppMessageRequest.getMessagingProduct());
    }

    @Test
    void createCancellationReasonSections() {
        List<SectionRequest> expectedSections = getExpectedCancellationReasonSections();
        List<SectionRequest> sectionRequestList = whatsAppInteractiveHelper.getCancellationReasonSections("OD12345");
        assertEquals(expectedSections, sectionRequestList);
    }


    @Test
    void createCatalogAndSupportSections() {
        List<InteractiveButtonRequest> expectedInteractiveButtonRequestList = getExpectedCatalogAndSupportButtons();
        List<InteractiveButtonRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.getCatalogAndSupportSections();
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void createCancellationButtons() {
        List<InteractiveButtonRequest> expectedInteractiveButtonRequestList = getExpectedCancellationButtons();
        List<InteractiveButtonRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.getCancellationButtons("OD123", "Yes", "No");
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void createWelcomeMessageButtons() {
        List<InteractiveButtonRequest> expectedInteractiveButtonRequestList = getExpectedWelcomeMessageButtons();
        List<InteractiveButtonRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.createWelcomeMessageButtons();
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void createTopCategorySections() {
        List<SectionRequest> expectedInteractiveButtonRequestList = getTopCategorySections();
        List<SectionRequest> interactiveButtonRequestList = whatsAppInteractiveHelper.createTopCategorySections();
        assertEquals(expectedInteractiveButtonRequestList, interactiveButtonRequestList);
    }

    @Test
    void testFetchAvailableSlotTimes() {
        String payload = "some_payload_data";
        LocalDate expectedDate = LocalDate.of(2024, 2, 20);
        GetSlotTimesRequest mockRequest = new GetSlotTimesRequest();
        mockRequest.setSlotDate(expectedDate);
        when(alterationServiceHelper.mapToGetSlotTimes(any(GetSlotTimesRequest.class))).thenReturn(getSlotAvailability());
        List<String> result = whatsAppInteractiveHelper.fetchAvailableSlotTimes(payload);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("10:00 AM"));
    }

    @Test
    void testFetchAvailableSlotDates_WithOrderItemId() {
        String recipientNumber = "919876543210";
        String payLoadData = "some_payload_IT76655_t_2024-07-09_T_11:00-12:00";
        String expectedPhone = "9876543210";
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByPhoneNumber(expectedPhone)).thenReturn(userEntity);
        when(alterationServiceHelper.mapToBookedSlotTimes(userEntity.getUserId())).thenReturn(getBookedSlotResponse());
        List<LocalDate> result = whatsAppInteractiveHelper.fetchAvailableSlotDates(recipientNumber, payLoadData);
        assertNotNull(result);
    }

    @Test
    void testFetchAvailableSlotDates_WithOrderItemId_isNull() {
        String recipientNumber = "919876543210";
        String payLoadData = "some_payload_t _2024-07-09_T_11:00-12:00";
        String expectedPhone = "9876543210";
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByPhoneNumber(expectedPhone)).thenReturn(userEntity);
        List<LocalDate> result = whatsAppInteractiveHelper.fetchAvailableSlotDates(recipientNumber, payLoadData);
        assertNotNull(result);
    }

    @Test
    void testFetchAvailableSlotDates_WithOrderItemId_isNotNull_withCurrentDate_withoutPhoneCode() {
        String recipientNumber = "96543210";
        String payLoadData = "some_payload_t _2025-02-21_T_11:00-12:00";
        String expectedPhone = "9876543210";
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByPhoneNumber(expectedPhone)).thenReturn(userEntity);
        List<LocalDate> result = whatsAppInteractiveHelper.fetchAvailableSlotDates(recipientNumber, payLoadData);
        assertNotNull(result);
    }

    private BookedSlotResponse getBookedSlotResponse() {
        List<BookedSlotTimes> bookedSlotTimesList = Arrays.asList(
                new BookedSlotTimes(
                        "SLOT123",
                        LocalDate.of(2024, 2, 20),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        new AddressResponse(),
                        getOrderItemResponses(),
                        true
                ),
                new BookedSlotTimes(
                        "SLOT456",
                        LocalDate.of(2024, 2, 21),
                        LocalTime.of(14, 0),
                        LocalTime.of(15, 0),
                        new AddressResponse(),
                        getOrderItemResponses(),
                        false
                )
        );

        BookedSlotResponse bookedSlotResponse = new BookedSlotResponse();
        bookedSlotResponse.setBookedSlotTimes(bookedSlotTimesList);
        return bookedSlotResponse;
    }

    private List<OrderItemResponse> getOrderItemResponses() {
        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();
        OrderItemResponse orderItemResponse = new OrderItemResponse();
        orderItemResponse.setOrderItemId("IT76655");
        orderItemResponse.setProductId("P001");
        orderItemResponse.setProductName("T-Shirt");
        orderItemResponse.setProductDescription("High-quality cotton T-shirt");
        orderItemResponse.setProductImage("https://example.com/tshirt.jpg");
        orderItemResponse.setQuantity(2);
        orderItemResponse.setUnitPrice(499.99);
        orderItemResponse.setProductColor("Red");
        orderItemResponse.setProductSize("L");
        orderItemResponse.setTotalAmount(999.98);
        orderItemResponse.setProductOfferPercentage(10.0);
        orderItemResponse.setReturnDaysPolicy(30);
        orderItemResponse.setDeliveryStatus(DeliveryStatus.SHIPPED);
        orderItemResponse.setDeliveryDate(LocalDateTime.now().plusDays(3));
        orderItemResponse.setCancelOrderDate(LocalDateTime.now().plusDays(6));
        orderItemResponse.setOrderStatus(OrderStatus.CONFIRMED);
        orderItemResponse.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItemResponse.setPinCodeInBengaluru(true);
        orderItemResponseList.add(orderItemResponse);
        return orderItemResponseList;
    }

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("123");
        userEntity.setGender("male");
        userEntity.setReferred(true);
        userEntity.setSlotEntities(new ArrayList<>());
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
        userEntity.setUserCustomizationEntityList(new ArrayList<>());
        userEntity.setLoyaltyPointsEntity(new LoyaltyPointsEntity());
        return userEntity;
    }


    private SlotTimes getSlotAvailability() {
        SlotTimes slotTimes = new SlotTimes();
        slotTimes.setTodayDate(LocalDate.of(2024, 2, 20));
        List<SlotAvailability> slotAvailabilities = new ArrayList<>();
        slotAvailabilities.add(new SlotAvailability("10:00 AM", true));
        slotAvailabilities.add(new SlotAvailability("11:00 AM", false));
        slotAvailabilities.add(new SlotAvailability("12:00 PM", true));
        slotTimes.setAvailableSlots(slotAvailabilities);
        return slotTimes;
    }


    private List<InteractiveButtonRequest> getExpectedCancellationButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        interactiveButtonRequestList.add(getButtons("yes_OD123", WhatsAppConstants.YES_BUTTON));
        interactiveButtonRequestList.add(getButtons("no_OD123", WhatsAppConstants.NO_BUTTON));
        return interactiveButtonRequestList;
    }

    private List<InteractiveButtonRequest> getExpectedWelcomeMessageButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        interactiveButtonRequestList.add(getButtons("start_button", WhatsAppConstants.START_BUTTON));
        interactiveButtonRequestList.add(getButtons("stop_button", WhatsAppConstants.STOP_BUTTON));
        return interactiveButtonRequestList;
    }

    private List<InteractiveButtonRequest> getExpectedCatalogAndSupportButtons() {
        List<InteractiveButtonRequest> interactiveButtonRequestList = new ArrayList<>();
        interactiveButtonRequestList.add(getButtons("1_product_category", WhatsAppConstants.TOP_CATEGORY));
        interactiveButtonRequestList.add(getButtons("2_trending_product", WhatsAppConstants.TRENDING_PRODUCT));
        interactiveButtonRequestList.add(getButtons("3_support_contact", WhatsAppConstants.CONTACT_US));
        return interactiveButtonRequestList;
    }

    private List<SectionRequest> getTopCategorySections() {
        List<RowRequest> rows = new ArrayList<>();
        rows.add(getSectionRequestRow("1_formal", WhatsAppConstants.FORMAL_PANTS));
        rows.add(getSectionRequestRow("2_causal", WhatsAppConstants.CASUAL_PANTS));
        rows.add(getSectionRequestRow("3_cargo", WhatsAppConstants.CARGO_PANTS));
        SectionRequest sectionRequest = new SectionRequest();
        sectionRequest.setTitle("tittle");
        sectionRequest.setRows(rows);
        return Collections.singletonList(sectionRequest);
    }

    private InteractiveButtonRequest getButtons(String id, String type) {
        InteractiveButtonRequest buttonRequest = new InteractiveButtonRequest();
        buttonRequest.setType("reply");
        buttonRequest.setReply(getButtonReply(id, type));
        return buttonRequest;
    }

    private ReplyRequest getButtonReply(String id, String type) {
        ReplyRequest replyRequest = new ReplyRequest();
        replyRequest.setId(id);
        replyRequest.setTitle(type);
        return replyRequest;
    }


    private List<SectionRequest> getExpectedCancellationReasonSections() {
        List<RowRequest> rows = new ArrayList<>();
        rows.add(getSectionRequestRow("1_" + "OD12345", "Inappropriate Size"));
        rows.add(getSectionRequestRow("2_" + "OD12345", "Order by Mistake"));
        rows.add(getSectionRequestRow("3_" + "OD12345", "Changed Mind"));
        rows.add(getSectionRequestRow("4_" + "OD12345", "Found a Better Price"));
        rows.add(getSectionRequestRow("5_" + "OD12345", "No Longer Needed"));
        rows.add(getSectionRequestRow("6_" + "OD12345", "Found Alternative"));
        rows.add(getSectionRequestRow("7_" + "OD12345", "Wrong Item/Color"));
        rows.add(getSectionRequestRow("8_" + "OD12345", "Product Quality Issues"));
        SectionRequest sectionRequest = new SectionRequest();
        sectionRequest.setTitle("tittle");
        sectionRequest.setRows(rows);
        return Collections.singletonList(sectionRequest);
    }

    private RowRequest getSectionRequestRow(String id, String reason) {
        RowRequest rowRequest = new RowRequest();
        rowRequest.setId(id);
        rowRequest.setTitle(reason);
        return rowRequest;
    }

    private InteractiveRequest getInterActiveList() {
        InteractiveRequest interactiveRequest = new InteractiveRequest();
        interactiveRequest.setType("list");
        interactiveRequest.setHeader(getInterActiveHeader());
        interactiveRequest.setBody(getInterActiveBody());
        interactiveRequest.setAction(getInterActiveAction());
        return interactiveRequest;
    }

    private ActionRequest getInterActiveAction() {
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setButton("button Text");
        actionRequest.setSections(getInterActiveSections());
        actionRequest.setButtons(getInterActiveButtons());
        return actionRequest;
    }

    private List<InteractiveButtonRequest> getInterActiveButtons() {
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

    private List<SectionRequest> getInterActiveSections() {
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

    private BodyRequest getInterActiveBody() {
        BodyRequest bodyRequest = new BodyRequest();
        bodyRequest.setText("body Text");
        return bodyRequest;
    }

    private HeaderRequest getInterActiveHeader() {
        HeaderRequest headerRequest = new HeaderRequest();
        headerRequest.setType("text");
        headerRequest.setText("header text");
        return headerRequest;
    }


    private WhatsAppMessageRequest getWhatsAppMessageRequest() {
        WhatsAppMessageRequest whatsAppMessageRequest = new WhatsAppMessageRequest();
        whatsAppMessageRequest.setMessagingProduct("whatsapp");
        whatsAppMessageRequest.setTo("9148671766");
        whatsAppMessageRequest.setType("interactive");
        InteractiveRequest interactiveRequest = getInterActiveList();
        whatsAppMessageRequest.setInteractive(interactiveRequest);
        return whatsAppMessageRequest;
    }

}