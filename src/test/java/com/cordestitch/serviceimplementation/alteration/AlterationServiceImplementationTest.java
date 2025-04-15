package com.cordestitch.serviceimplementation.alteration;

import com.cordestitch.entity.alteration.SlotEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.product.*;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.exception.alteration.SlotAlreadyBookedException;
import com.cordestitch.exception.alteration.SlotDataNotFoundException;
import com.cordestitch.exception.user.AddressNotFoundException;
import com.cordestitch.exception.user.UserDetailsMissMatchException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.alteration.SlotRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.user.AddressRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.alteration.BookMeasurementSlotRequest;
import com.cordestitch.request.alteration.BookSlotRequest;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.request.alteration.RescheduleOrCancelRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.alteration.*;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.service.serviceimplementation.alteration.AlterationServiceHelper;
import com.cordestitch.service.serviceimplementation.alteration.AlterationServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;


class AlterationServiceImplementationTest {

    @InjectMocks
    private AlterationServiceImplementation alterationServiceImplementation;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Generator generator;

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private AlterationServiceHelper alterationServiceHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrderItems_Success() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();

        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setSlotEntity(null);
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(alterationServiceHelper.mapToOrderItemResponse(anyList())).thenReturn(getOrderItemResponses());
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress);
        assertEquals("123", orderEntities.getFirst().getOrderItemEntities().getFirst().getOrderItemId(), response.getOrderItemResponses().getFirst().getOrderItemId());
    }

    @Test
    void getOrderItems_Success_When_ReturnPolicy_Exceeded_With_SlotEntity_Null() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();

        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setSlotEntity(null);
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(alterationServiceHelper.mapToOrderItemResponse(anyList())).thenReturn(getOrderItemResponses());
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress);
        assertNotNull(response);
    }

    @Test
    void getOrderItems_Success_When_Product_Is_Not_Present() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setSlotEntity(null);
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        when(alterationServiceHelper.mapToOrderItemResponse(anyList())).thenReturn(getOrderItemResponses());
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress);
        assertEquals("123", orderEntities.getFirst().getOrderItemEntities().getFirst().getOrderItemId(), response.getOrderItemResponses().getFirst().getOrderItemId());
    }

    @Test
    void getOrderItems_Success_When_ReturnPolicyExceeds() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().plusDays(8));
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress);
        assertTrue("", response.getOrderItemResponses().isEmpty());
    }

    @Test
    void getOrderItems_Success_When_PhoneNumber_Is_Null() {
        String userId = "user1";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().plusDays(8));
        when(userRepository.findByUserIdAndEmailAddress(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, null, emailAddress);
        assertTrue("", response.getOrderItemResponses().isEmpty());
    }

    @Test
    void getOrderItems_Exception_When_PhoneNumber_And_EmailAddress_Is_Null() {
        String userId = "user1";
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().plusDays(8));
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, () -> alterationServiceImplementation.getOrderItems(userId, null, null));
        assertEquals(Constants.USER_DATA_MISSING_ERROR, Constants.USER_DATA_MISSING_ERROR, exception.getMessage());
    }

    @Test
    void getOrderItems_Exception_When_PhoneNumber_And_EmailAddress_Is_Empty() {
        String userId = "user1";
        String phoneNumber = "";
        String emailAddress = "";
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().plusDays(8));
        when(userRepository.findByUserIdAndEmailAddress(any(), any())).thenReturn(Optional.of(getUserEntity()));
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, () -> alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress));
        assertEquals(Constants.USER_DATA_MISSING_ERROR, Constants.USER_DATA_MISSING_ERROR, exception.getMessage());
    }

    @Test
    void getOrderItems_Exception_When_PhoneNumber_Is_Not_Null() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "";
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().plusDays(8));
        when(userRepository.findByUserIdAndEmailAddress(any(), any())).thenReturn(Optional.of(getUserEntity()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress));
        assertEquals(Constants.USER_NOT_FOUND, Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getOrderItems_Exception_When_EmailAddress_Is_Not_Null() {
        String userId = "user1";
        String phoneNumber = "";
        String emailAddress = "user@gmail.com";
        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().plusDays(8));
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(getUserEntity()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress));
        assertEquals(Constants.USER_NOT_FOUND, Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getOrderItems_Success_When_FilteredOrderEntities_Are_Empty() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();

        List<OrderEntity> orderEntities = getOrderEntities();
        orderEntities.getFirst().getAddressEntity().setPinCode("573103");
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress);
        assertTrue("", response.getOrderItemResponses().isEmpty());
    }

    @Test
    void getOrderItems_Exception_Order_Not_Found() {
        String userId = "user1";
        String phoneNumber = "1234567890";
        String emailAddress = "user@gmail.com";
        UserEntity userEntity = getUserEntity();

        List<OrderEntity> orderEntities = new ArrayList<>();
        when(userRepository.findByUserIdAndPhoneNumber(any(), any())).thenReturn(Optional.of(userEntity));
        when(orderRepository.findAllByUserEntityUserId(any())).thenReturn(orderEntities);
        AlterationResponse response = alterationServiceImplementation.getOrderItems(userId, phoneNumber, emailAddress);
        assertTrue("", response.getOrderItemResponses().isEmpty());
    }

    @Test
    void getSlotTimes_Success_When_Addresses_Size_Is_LessThen1() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(getListOfOrderItemEntities(getOrderEntity()));
        when(alterationServiceHelper.mapToGetSlotTimes(getSlotTimesRequest())).thenReturn(getSlotTimesResponse());
        SlotTimes response = alterationServiceImplementation.getSlotTimes(request);
        assertEquals("", request.getSlotDate(), response.getTodayDate());
    }

    private SlotTimes getSlotTimesResponse() {
        SlotTimes slotTimes = new SlotTimes();
        slotTimes.setTodayDate(LocalDate.now());
        List<SlotAvailability> slots = getSlotAvailability();
        slotTimes.setAvailableSlots(slots);
        return slotTimes;
    }

    private List<SlotAvailability> getSlotAvailability() {
        List<SlotAvailability> slots = new ArrayList<>();
        SlotAvailability slot = new SlotAvailability();
        slot.setSlotTime("10:00AM-11:00AM");
        slot.setAvailable(true);
        slots.add(slot);
        return slots;
    }

    private List<SlotEntity> getSlotEntities() {
        List<SlotEntity> slotEntities = new ArrayList<>();
        SlotEntity slotEntity = new SlotEntity();
        slotEntity.setSlotId("456");
        slotEntity.setAddressEntity(getAddressEntity());
        slotEntity.setSlotDate(LocalDate.now().plusDays(3));
        slotEntity.setSlotBooked(true);
        slotEntity.setUserEntity(getUserEntity());
        slotEntity.setEndTime(LocalTime.of(11, 0));
        slotEntity.setStartTime(LocalTime.of(10, 0));
        slotEntity.setOrderItems(new ArrayList<>());
        slotEntities.add(slotEntity);
        return slotEntities;

    }

    @Test
    void bookSlotTimes_Success() {
        BookSlotRequest request = getBookSlotRequest();
        request.setOrderItemIds(List.of("123", "456"));
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        List<OrderItemEntity> orderItems = getListOfOrderItemEntities(getOrderEntity());
        when(alterationServiceHelper.getOrderItemsByIds(any())).thenReturn(orderItems);
        when(orderItemRepository.findByOrderItemIdIn(anyList())).thenReturn(orderItems);
        Map<String, List<OrderItemEntity>> orderItemsByAddress = new HashMap<>();
        orderItemsByAddress.put("address1", orderItems);
        when(alterationServiceHelper.groupOrderItemsByAddress(orderItems)).thenReturn(orderItemsByAddress);
        SuccessResponse response = alterationServiceImplementation.bookSlotTimes(request);
        assertEquals(Constants.SLOT_BOOKED_SUCCESSFULLY, Constants.SLOT_BOOKED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void bookSlotTimes_Exception_User_Not_Found() {
        BookSlotRequest request = getBookSlotRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> alterationServiceImplementation.bookSlotTimes(request));
        assertEquals(Constants.USER_NOT_FOUND, Constants.USER_NOT_FOUND, exception.getMessage());
    }


    @Test
    void rescheduleOrCancelSlotTimes_Success() {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        when(slotRepository.findByUserEntityUserIdAndSlotDateAndStartTimeAndEndTime(any(), any(), any(), any())).thenReturn(Optional.of(getSlotEntity()));
        SuccessResponse response = alterationServiceImplementation.rescheduleOrCancelSlotTimes(request);
        assertEquals(Constants.SLOT_TIME_RESCHEDULED_SUCCESSFULLY, HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void rescheduleOrCancelSlotTimes_Success_When_Request_Reschedule_Is_False() {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        request.setReschedule(false);
        List<OrderItemEntity> orderItems = getListOfOrderItemEntities(getOrderEntity());
        when(slotRepository.findByUserEntityUserIdAndSlotDateAndStartTimeAndEndTime(any(), any(), any(), any())).thenReturn(Optional.of(getSlotEntity()));
        when(orderItemRepository.findBySlotEntitySlotId(any())).thenReturn(orderItems);
        SuccessResponse response = alterationServiceImplementation.rescheduleOrCancelSlotTimes(request);
        assertEquals(Constants.CANCELLED_SLOT_TIME_SUCCESSFULLY, HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void rescheduleOrCancelSlotTimes_Exception_When_SlotEntityOptional_IsAbsent() {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        when(slotRepository.findByUserEntityUserIdAndSlotDateAndStartTimeAndEndTime(any(), any(), any(), any())).thenReturn(Optional.empty());
        SlotDataNotFoundException exception = assertThrows(SlotDataNotFoundException.class, () -> alterationServiceImplementation.rescheduleOrCancelSlotTimes(request));
        assertEquals(Constants.SLOT_DATA_NOT_FOUND, String.format(Constants.SLOT_DATA_NOT_FOUND, request.getSlotDate(), request.getStartTime(), request.getEndTime()), exception.getMessage());
    }


    @Test
    void rescheduleOrCancelSlotTimes_when_orderItemId_isNull() {
        RescheduleOrCancelRequest request = getRescheduleOrCancelRequest();
        SlotEntity slotEntity = getSlotEntity();
        slotEntity.setOrderItems(Collections.emptyList());
        when(slotRepository.findByUserEntityUserIdAndSlotDateAndStartTimeAndEndTime(any(), any(), any(), any())).thenReturn(Optional.of(slotEntity));
        SuccessResponse response = alterationServiceImplementation.rescheduleOrCancelSlotTimes(request);
        assertEquals(Constants.SLOT_TIME_RESCHEDULED_SUCCESSFULLY, HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void getBookedSlotTimes_Success() {
        String userId = "user1";
        when(slotRepository.findAllByUserEntityUserId(any())).thenReturn(getSlotEntities());
        when(modelMapper.map(any(), any())).thenReturn(new AddressResponse());
        when(alterationServiceHelper.mapToBookedSlotTimes(userId)).thenReturn(new BookedSlotResponse());
        BookedSlotResponse response = alterationServiceImplementation.getBookedSlotTimes(userId);
        assertNotNull(response);
    }

    @Test
    void bookMeasurementSlot_Success() {
        BookMeasurementSlotRequest request = getBookMeasurementSlotRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(slotRepository.findBySlotDateAndStartTimeAndEndTime(any(), any(), any())).thenReturn(Optional.empty());
        when(addressRepository.findByAddressId(any())).thenReturn(Optional.of(getAddressEntity()));
        SuccessResponse response = alterationServiceImplementation.bookMeasurementSlot(request);
        assertEquals(Constants.SLOT_BOOKED_SUCCESSFULLY, Constants.SLOT_BOOKED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void bookMeasurementSlot_Exception_When_AddressEntity_Is_Empty() {
        BookMeasurementSlotRequest request = getBookMeasurementSlotRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(slotRepository.findBySlotDateAndStartTimeAndEndTime(any(), any(), any())).thenReturn(Optional.empty());
        when(addressRepository.findByAddressId(any())).thenReturn(Optional.empty());
        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> alterationServiceImplementation.bookMeasurementSlot(request));
        assertEquals(Constants.ADDRESS_NOT_FOUND, Constants.ADDRESS_NOT_FOUND, exception.getMessage());
    }

    @Test
    void bookMeasurementSlot_Exception_When_SlotEntity_Is_Empty() {
        BookMeasurementSlotRequest request = getBookMeasurementSlotRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(slotRepository.findBySlotDateAndStartTimeAndEndTime(any(), any(), any())).thenReturn(Optional.of(getSlotEntity()));
        SlotAlreadyBookedException exception = assertThrows(SlotAlreadyBookedException.class, () -> alterationServiceImplementation.bookMeasurementSlot(request));
        assertEquals(Constants.SLOT_ALREADY_BOOKED, String.format(Constants.SLOT_ALREADY_BOOKED, request.getSlotDate(), request.getStartTime(), request.getEndTime()), exception.getMessage());
    }

    @Test
    void bookMeasurementSlot_Exception_When_UserEntity_Is_Null() {
        BookMeasurementSlotRequest request = getBookMeasurementSlotRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> alterationServiceImplementation.bookMeasurementSlot(request));
        assertEquals(Constants.USER_NOT_FOUND, Constants.USER_NOT_FOUND + ":{}" + request.getUserId(), exception.getMessage());
    }

    private BookMeasurementSlotRequest getBookMeasurementSlotRequest() {
        BookMeasurementSlotRequest request = new BookMeasurementSlotRequest();
        request.setAddressId("address1");
        request.setUserId("user1");
        request.setSlotDate(LocalDate.now().plusDays(1));
        request.setEndTime(LocalTime.now().plusHours(5));
        request.setStartTime(LocalTime.now());
        return request;
    }

    private RescheduleOrCancelRequest getRescheduleOrCancelRequest() {
        RescheduleOrCancelRequest request = new RescheduleOrCancelRequest();
        request.setReschedule(true);
        request.setEndTime(LocalTime.now().plusHours(4));
        request.setStartTime(LocalTime.now());
        request.setUserId("user1");
        request.setSlotDate(LocalDate.now());
        request.setNewEndTime(LocalTime.now().plusHours(6));
        request.setNewStartTime(LocalTime.now().plusHours(1));
        request.setNewSlotDate(LocalDate.now().plusDays(1));
        return request;
    }

    private BookSlotRequest getBookSlotRequest() {
        BookSlotRequest bookSlotRequest = new BookSlotRequest();
        bookSlotRequest.setSlotDate(LocalDate.now());
        bookSlotRequest.setUserId("user1");
        bookSlotRequest.setEndTime(LocalTime.now());
        bookSlotRequest.setStartTime(LocalTime.now().minusHours(4));
        bookSlotRequest.setOrderItemIds(new ArrayList<>());
        return bookSlotRequest;
    }

    private GetSlotTimesRequest getSlotTimesRequest() {
        GetSlotTimesRequest request = new GetSlotTimesRequest();
        request.setSlotDate(LocalDate.now());
        request.setOrderItemIds(List.of("order1", "order2"));
        return request;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setProductId("1");
        product.setProductName("Pant");
        product.setProductStatus("Pending");
        product.setProductDescription("Formal pant");
        product.setProductPattern("plain");
        product.setProductMaterialType("polyester");
        product.setProductOfferPercentage(5.0);
        product.setSubCategory(new SubCategory());
        product.setStockQuantities(List.of(getStockQuantity()));
        product.setProductImages(getImageMap());
        product.setVideoUrl("video.com");
        return product;
    }

    private List<ProductImage> getImageMap() {
        List<ProductImage> productDataResponses = new ArrayList<>();
        ProductImage response = new ProductImage();
        response.setColorName("Blue");
        response.setImageUrl("image1");
        response.setImagePriority(1);
        productDataResponses.add(response);

        return productDataResponses;
    }

    private StockQuantity getStockQuantity() {
        StockQuantity stockQuantity = new StockQuantity();
        stockQuantity.setSize(32);
        stockQuantity.setProductPrice(100.0);
        stockQuantity.setProduct(new Product());
        stockQuantity.setStockId("11");
        stockQuantity.setColorQuantities(getColorQuantity());
        return stockQuantity;
    }

    private List<ColorQuantity> getColorQuantity() {
        List<ColorQuantity> list = new ArrayList<>();
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("1");
        colorQuantity.setStockQuantity(new StockQuantity());
        colorQuantity.setColor("white");
        colorQuantity.setColorCode("#ffffff");
        colorQuantity.setQuantity(5);
        list.add(colorQuantity);
        return list;
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

    private List<OrderEntity> getOrderEntities() {
        List<OrderEntity> orderEntities = new ArrayList<>();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId("123");
        orderEntity.setUserEntity(new UserEntity());
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderEntity.setPaymentMethod("Card");
        orderEntity.setTotalAmount(150.0);
        orderEntity.setOrderItemEntities(getListOfOrderItemEntities(orderEntity));
        orderEntity.setPaymentEntity(getPaymentEntity(orderEntity));
        orderEntity.setAddressEntity(getAddressEntity());
        orderEntities.add(orderEntity);
        return orderEntities;
    }

    private AddressEntity getAddressEntity() {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddressId("10");
        addressEntity.setLandMark("near Government high school");
        addressEntity.setTypeOfAddress("home");
        addressEntity.setPinCode("560004");
        addressEntity.setStreetName("kogilu");
        addressEntity.setStateName("karnataka");
        addressEntity.setUserEntity(getUserEntity());
        addressEntity.setOrderEntities(List.of(new OrderEntity()));
        addressEntity.setBuildingName("ramanashree");
        addressEntity.setCityName("yelahanka");
        addressEntity.setCountryName("India");
        addressEntity.setFirstName("jay");
        addressEntity.setLastName("prakash");
        addressEntity.setPhoneNumber("1234567890");
        return addressEntity;
    }

    private PaymentEntity getPaymentEntity(OrderEntity orderEntity) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderEntity(orderEntity);
        paymentEntity.setPaymentDate(LocalDateTime.now());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentMethod("Card");
        paymentEntity.setUserId("1");
        paymentEntity.setRazorPayOrderId("razorpay123");
        paymentEntity.setPaymentId("pay123");
        paymentEntity.setRazorPayPaymentId("rpayment123");
        paymentEntity.setTotalAmount(150.0);
        paymentEntity.setRefundEntities(getListRefundEntities(paymentEntity,paymentEntity.getOrderEntity().getOrderItemEntities().getFirst()));
        return paymentEntity;
    }

    private List<RefundEntity> getListRefundEntities(PaymentEntity paymentEntity, OrderItemEntity first) {
        List<RefundEntity> refundEntities = new ArrayList<>();
        RefundEntity refundEntity = new RefundEntity();
        refundEntity.setRefundId("r123");
        refundEntity.setRefundDate(LocalDateTime.now());
        refundEntity.setRefundStatus(RefundStatus.NOT_REQUESTED);
        refundEntity.setRefundAmount(10.0);
        refundEntity.setRefundIdOrPayoutId("pout_35gfev2j21njk");
        refundEntity.setRefundType(RefundType.PRODUCT);
        refundEntity.setPaymentEntity(paymentEntity);
        refundEntity.setOrderItemEntity(first);
        refundEntities.add(refundEntity);
        return refundEntities;
    }


    private List<OrderItemEntity> getListOfOrderItemEntities(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(orderEntity);
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setProductSize(30);
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(150.0);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        orderItemEntity.setSlotEntity(new SlotEntity());
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
    }

    private SlotEntity getSlotEntity() {
        SlotEntity slotEntity = new SlotEntity();
        slotEntity.setSlotId("123");
        slotEntity.setAddressEntity(getAddressEntity());
        slotEntity.setSlotDate(LocalDate.now());
        slotEntity.setSlotBooked(true);
        slotEntity.setUserEntity(getUserEntity());
        slotEntity.setEndTime(LocalTime.now());
        slotEntity.setStartTime(LocalTime.now().minusHours(4));
        slotEntity.setOrderItems(getListOfOrderItemEntities(getOrderEntity()));
        return slotEntity;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId("123");
        orderEntity.setUserEntity(new UserEntity());
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setOrderItemEntities(getListOfOrderItemEntities(orderEntity));
        orderEntity.setPaymentEntity(getPaymentEntity(orderEntity));
        orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderEntity.setPaymentMethod("Card");
        orderEntity.setAddressEntity(getAddressEntity());
        orderEntity.setTotalAmount(150.0);
        return orderEntity;
    }

    private List<OrderItemResponse> getOrderItemResponses() {
        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();
        OrderItemResponse orderItemResponse = new OrderItemResponse();
        orderItemResponse.setOrderItemId("123");
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
}