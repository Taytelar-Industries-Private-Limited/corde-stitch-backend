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
import com.cordestitch.exception.alteration.DateConversionException;
import com.cordestitch.exception.alteration.MultipleAddressesFoundException;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.repository.alteration.SlotRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.alteration.GetSlotTimesRequest;
import com.cordestitch.response.alteration.BookedSlotResponse;
import com.cordestitch.response.alteration.SlotTimes;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.service.serviceimplementation.alteration.AlterationServiceHelper;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class AlterationServiceHelperTest {

    @InjectMocks
    private AlterationServiceHelper alterationServiceHelper;

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSlotTimes_Success_When_Addresses_Size_Is_GreaterThen1() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(getOrderEntity());
        orderItemEntities.add(getOrderItemEntityA());
        orderItemEntities.get(1).getOrderEntity().setAddressEntity(new AddressEntity());
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        MultipleAddressesFoundException exception = assertThrows(MultipleAddressesFoundException.class, () -> alterationServiceHelper.mapToGetSlotTimes(request));
        assertEquals(Constants.SELECT_ERROR_MESSAGE, Constants.SELECT_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void getSlotTimes_Exception_When_OrderItems_Is_Empty() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        List<OrderItemEntity> orderItemEntities = List.of();
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> alterationServiceHelper.mapToGetSlotTimes(request));
        assertEquals(Constants.ORDER_ITEM_NOT_FOUND, Constants.ORDER_ITEM_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getSlotTimes_Success_When_SlotDates_Are_Null() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        request.setSlotDate(null);
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(getOrderEntity());
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        when(slotRepository.findBySlotDate(LocalDate.now())).thenReturn(new ArrayList<>());
        SlotTimes response = alterationServiceHelper.mapToGetSlotTimes(request);
        assertEquals("", LocalDate.now(), response.getTodayDate());
    }

    @Test
    void getSlotTimes_Success_When_OrderItemIds_Is_Null() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        request.setOrderItemIds(null);
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(getOrderEntity());
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        when(slotRepository.findBySlotDate(LocalDate.now())).thenReturn(new ArrayList<>());
        SlotTimes response = alterationServiceHelper.mapToGetSlotTimes(request);
        assertEquals("", LocalDate.now(), response.getTodayDate());
    }

    @Test
    void getSlotTimes_Success_When_OrderItemIds_Is_Empty() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        request.setOrderItemIds(new ArrayList<>());
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(getOrderEntity());
        List<SlotEntity> slotEntities = getSlotEntities();
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        when(slotRepository.findBySlotDate(LocalDate.now())).thenReturn(slotEntities);
        SlotTimes response = alterationServiceHelper.mapToGetSlotTimes(request);
        assertEquals("", LocalDate.now(), response.getTodayDate());
    }

    @Test
    void getSlotTimes_Success_When_SlotTime_Are_Booked() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(getOrderEntity());
        List<SlotEntity> slotEntities = getSlotEntities();
        slotEntities.add(getSlotEntity());
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        when(slotRepository.findBySlotDate(LocalDate.now())).thenReturn(slotEntities);
        SlotTimes response = alterationServiceHelper.mapToGetSlotTimes(request);
        assertEquals("", LocalDate.now(), response.getTodayDate());
    }

    @Test
    void getSlotTimes_Success_SlotTime_Are_Booked_When_SlotDate_Is_Not_EqualTo_LocalDateNow() {
        GetSlotTimesRequest request = getSlotTimesRequest();
        request.setSlotDate(LocalDate.now().plusDays(1));
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(getOrderEntity());
        List<SlotEntity> slotEntities = getSlotEntities();
        slotEntities.add(getSlotEntity());
        when(orderItemRepository.findByOrderItemIdIn(any())).thenReturn(orderItemEntities);
        when(slotRepository.findBySlotDate(LocalDate.now())).thenReturn(slotEntities);
        SlotTimes response = alterationServiceHelper.mapToGetSlotTimes(request);
        assertEquals("", LocalDate.now().plusDays(1), response.getTodayDate());
    }

    @Test
    void getBookedSlotTimes_Exception_Currently_Does_Not_Booked_Any_Slots() {
        String userId = "user1";
        when(slotRepository.findAllByUserEntityUserId(any())).thenReturn(List.of());
        BookedSlotResponse response = alterationServiceHelper.mapToBookedSlotTimes(userId);
        Assertions.assertEquals(new ArrayList<>(), response.getBookedSlotTimes());
    }

    @Test
    void bookSlotTimes_Success() {

        String userId = "user1";
        when(slotRepository.findAllByUserEntityUserId(any())).thenReturn(getSlotEntities());
        when(modelMapper.map(any(), any())).thenReturn(new AddressResponse());
        BookedSlotResponse response = alterationServiceHelper.mapToBookedSlotTimes(userId);
        assertNotNull(response);
    }

    @Test
    void mapToOrderItemResponse() {

        List<OrderItemResponse> excpectedOrderItemResponseList = getOrderItemResponses();
        when(productRepository.findByProductId(anyString())).thenReturn(Optional.of(getProduct()));
        List<OrderItemResponse> actualOrderItemList = alterationServiceHelper.mapToOrderItemResponse(getOrderItemList());
        Assertions.assertEquals(excpectedOrderItemResponseList.getFirst().getOrderItemId(), actualOrderItemList.getFirst().getOrderItemId());

    }

    @Test
    void getSlotStartAndEndTime() {
        SlotEntity slotEntity = getSlotEntity();
        slotEntity.setStartTime(LocalTime.of(10, 0));
        slotEntity.setEndTime(LocalTime.of(12, 0));

        String slotTime = alterationServiceHelper.getSlotStartAndEndTime(slotEntity);

        Assertions.assertEquals("10:00AM-12:00PM", slotTime);
    }

    @Test
    void getOrderItemIds_ShouldReturnSingleOrderItemId() {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderItemId("OI123");
        String singleOrderItemId = alterationServiceHelper.getOrderItemIds(List.of(orderItem));

        Assertions.assertEquals("OI123", singleOrderItemId);
    }

    @Test
    void getOrderItemIds_ShouldReturnCommaSeparatedOrderItemIds() {

        OrderItemEntity orderItem1 = new OrderItemEntity();
        orderItem1.setOrderItemId("OI123");
        OrderItemEntity orderItem2 = new OrderItemEntity();
        orderItem2.setOrderItemId("OI456");
        OrderItemEntity orderItem3 = new OrderItemEntity();
        orderItem3.setOrderItemId("OI789");
        List<OrderItemEntity> orderItems = Arrays.asList(orderItem1, orderItem2, orderItem3);
        String multipleOrderItemId = alterationServiceHelper.getOrderItemIds(orderItems);

        Assertions.assertEquals("OI123, OI456, OI789", multipleOrderItemId);
    }

    @Test
    void shouldThrowExceptionForIncompleteTime() {
        String invalidTime = "25";

        assertThatThrownBy(() -> alterationServiceHelper.convertTo12HourFormat(invalidTime))
                .isInstanceOf(DateConversionException.class)
                .hasMessageContaining("Invalid time format");
    }

    @Test
    void shouldHandleValid12HourFormat() {
        String valid12HourTime = "10:30am";

        String result = alterationServiceHelper.convertTo12HourFormat(valid12HourTime);

        assertThat(result).isEqualTo("10:30AM");
    }

    @Test
    void shouldReturnFalseWhenCurrentTimeIsBetweenSlotTimes() {

        LocalTime slotStartTime = LocalTime.of(10, 0);
        LocalTime slotEndTime = LocalTime.of(12, 0);
        LocalTime currentTime = LocalTime.of(11, 0);

        boolean result = alterationServiceHelper.isSlotAvailableToday(slotStartTime, slotEndTime, currentTime);
        assertThat(result).isFalse();

    }


    private List<OrderItemEntity> getOrderItemList() {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P001");
        orderItemEntity.setQuantity(2);
        orderItemEntity.setUnitPrice(499.99);
        orderItemEntity.setProductColor("Red");
        orderItemEntity.setProductSize(21);
        orderItemEntity.setProductOfferPercentage(10.0);
        orderItemEntity.setTotalAmount(999.98);
        orderItemEntity.setReturnDaysPolicy(30);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.SHIPPED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(3));
        orderItemEntity.setCancelDate(LocalDateTime.now().plusDays(6));
        orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderItemEntity.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItemEntity.setOrderEntity(getOrderEntity());
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
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
        paymentEntity.setRefundEntities(getListRefundEntities(paymentEntity, paymentEntity.getOrderEntity().getOrderItemEntities().getFirst()));
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

    private OrderItemEntity getOrderItemEntityA() {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(getOrderEntity());
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(150.0);
        return orderItemEntity;
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
        slotEntity.setOrderItems(new ArrayList<>());
        return slotEntity;
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

    private GetSlotTimesRequest getSlotTimesRequest() {
        GetSlotTimesRequest request = new GetSlotTimesRequest();
        request.setSlotDate(LocalDate.now());
        request.setOrderItemIds(List.of("order1", "order2"));
        return request;
    }
}