package com.cordestitch.serviceimplementation.payment;

import com.cordestitch.service.serviceimplementation.payment.PaymentServiceHelper;
import com.cordestitch.service.serviceimplementation.payment.PaymentServiceImpl;
import com.razorpay.*;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.exception.payment.*;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.payment.CardRepository;
import com.cordestitch.repository.payment.PaymentRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.payment.CardRequest;
import com.cordestitch.request.payment.PaymentRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.payment.CardDetailsResponse;
import com.cordestitch.response.payment.PaymentData;
import com.cordestitch.response.payment.PaymentResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.service.serviceimplementation.order.OrderServiceImplementation;
import com.cordestitch.util.Generator;
import org.apache.catalina.mapper.Mapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import com.cordestitch.util.Constants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private OrderServiceImplementation orderServiceImplementation;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private Generator generator;

    @Mock
    private Mapper mapper;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private PaymentServiceHelper paymentServiceHelper;

    @Mock
    private LoyaltyPointsService loyaltyPointsService;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private RefundClient refundClient;

    @Mock
    private WhatsAppService whatsAppService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(paymentService, "keyID", "rzp_test_nHgaZ8pP0SqyOm");
        ReflectionTestUtils.setField(paymentService, "keySecret", "y02XoqnCoIJbVHHvPGKcQPCd");
        ReflectionTestUtils.setField(razorpayClient, "payments", paymentClient);
        ReflectionTestUtils.setField(razorpayClient, "refunds", refundClient);

    }


    @Test
    void CreatePayment_Success() {
        PaymentRequest paymentRequest = getPaymentRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        PaymentResponse response = paymentService.createPayment(paymentRequest);
        assertEquals(Constants.SUCCESS, response.getStatus());
        assertEquals(Constants.PAYMENT_CREATED_SUCCESSFULLY, response.getMessage());
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
    }

    @Test
    void CreatePayment_Success_When_OrderEntityDataIsNull() {
        PaymentRequest paymentRequest = getPaymentRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(orderRepository.findByOrderId(any())).thenReturn(null);
        assertThrows(OrderNotFoundException.class, ()-> paymentService.createPayment(paymentRequest));
    }

    @Test
    void createPayment_ShouldReturnError_WhenRazorpayExceptionOccurs() throws Exception {
        PaymentRequest paymentRequest = getPaymentRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        ReflectionTestUtils.setField(paymentService, "keyID", "rzp_test_nHgaZ8pP0SqyOma");
        ReflectionTestUtils.setField(paymentService, "keySecret", "y02XoqnCoIJbVHHvPGKcQPCda");
        when(orderClient.create(any(JSONObject.class))).thenThrow(new RazorpayException("Simulated Razorpay Exception"));
        assertThrows(PaymentProcessingException.class, ()-> paymentService.createPayment(paymentRequest));
    }

    @Test
    void createPayment_Exception_User_Not_Found() {
        PaymentRequest paymentRequest = getPaymentRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> paymentService.createPayment(paymentRequest));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());

    }


    @Test
    void verifyRazorpaySignature_Success() {
        PaymentData paymentData = getPaymentData();
        when(paymentRepository.findByRazorPayOrderId(any())).thenReturn(new PaymentEntity());
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        SuccessResponse response = paymentService.verifyRazorpaySignature(paymentData);
        assertEquals(Constants.PAYMENT_SUCCESS, response.getMessage());
    }

    @Test
    void verifyRazorpaySignature_Success_When_OrderItemEntity_Is_Null() {
        PaymentData paymentData = getPaymentData();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderItemEntities(new ArrayList<>());
        when(paymentRepository.findByRazorPayOrderId(any())).thenReturn(new PaymentEntity());
        when(orderRepository.findByOrderId(any())).thenReturn(orderEntity);
        SuccessResponse response = paymentService.verifyRazorpaySignature(paymentData);
        assertEquals(Constants.PAYMENT_SUCCESS, response.getMessage());
    }

    @Test
    void verifyRazorpaySignature_When_Payment_Failed() {
        PaymentData paymentData = new PaymentData();
        when(paymentRepository.findByRazorPayOrderId(any())).thenReturn(new PaymentEntity());
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        SuccessResponse response = paymentService.verifyRazorpaySignature(paymentData);
        assertEquals(Constants.PAYMENT_FAILED, response.getMessage());
    }

    @Test
    void testRetryPayment_Successful() {
        PaymentRequest paymentRequest = getPaymentRequest();
        Optional<PaymentEntity> paymentEntity = getPaymentEntity();
        paymentEntity.get().setPaymentStatus(PaymentStatus.FAILED);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(paymentRepository.findByOrderEntityOrderId(any())).thenReturn(paymentEntity);
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        PaymentResponse response = paymentService.retryPayment(paymentRequest);
        assertEquals(Constants.SUCCESS, response.getStatus());
    }

    @Test
    void testRetryPayment_Throws_PaymentProcessingException() throws RazorpayException {
        PaymentRequest paymentRequest = getPaymentRequest();
        Optional<PaymentEntity> paymentEntity = getPaymentEntity();
        paymentEntity.get().setPaymentStatus(PaymentStatus.FAILED);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(paymentRepository.findByOrderEntityOrderId(any())).thenReturn(paymentEntity);
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        ReflectionTestUtils.setField(paymentService, "keyID", "rzp_test_nHgaZ8pP0SqyOma");
        ReflectionTestUtils.setField(paymentService, "keySecret", "y02XoqnCoIJbVHHvPGKcQPCda");
        when(orderClient.create(any(JSONObject.class))).thenThrow(new RazorpayException("Simulated Razorpay Exception"));
        assertThrows(PaymentProcessingException.class, ()-> paymentService.retryPayment(paymentRequest));
    }

    @Test
    void testRetryPayment_Throws_PaymentNotFoundException() {
        PaymentRequest paymentRequest = getPaymentRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(paymentRepository.findByOrderEntityOrderId(any())).thenReturn(Optional.empty());
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, () -> paymentService.retryPayment(paymentRequest));
        assertEquals(Constants.PAYMENT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testRetryPayment_Throws_PaymentAlreadyConfirmedException() {
        PaymentRequest paymentRequest = getPaymentRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(paymentRepository.findByOrderEntityOrderId(any())).thenReturn(getPaymentEntity());
        when(orderRepository.findByOrderId(any())).thenReturn(getOrderEntity());
        PaymentAlreadyConfirmedException exception = assertThrows(PaymentAlreadyConfirmedException.class, () -> paymentService.retryPayment(paymentRequest));
        assertEquals(Constants.PAYMENT_ALREADY_CONFIRMED, exception.getMessage());
    }

    @Test
    void addCard_Success() {
        CardRequest cardRequest = getCardRequest();
        CardEntity cardEntity = getCardEntity();
        when(cardRepository.save(any(CardEntity.class))).thenReturn(cardEntity);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        SuccessResponse response = paymentService.addCard(cardRequest);
        assertEquals(Constants.CARD_ADDED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void addCard_Exception_User_Not_Found() {
        CardRequest cardRequest = getCardRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> paymentService.addCard(cardRequest));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getAllCards_Success() {
        String userId = "user1";
        CardEntity cardEntity = getCardEntity();
        when(cardRepository.findByUserEntityUserId(userId)).thenReturn(List.of(cardEntity));
        List<CardDetailsResponse> response = paymentService.getAllCards(userId);
        assertFalse(response.isEmpty());
    }


    @Test
    void deleteCard_Success() {
        String cardID = "card1";
        String userId = "user1";
        when(cardRepository.findByCardIdAndUserEntityUserId(cardID, userId)).thenReturn(Optional.of(new CardEntity()));
        SuccessResponse response = paymentService.deleteCard(cardID, userId);
        assertEquals(Constants.CARD_DELETE_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void deleteCard_Exception(){
        String cardID = "card1";
        String userId = "user1";
        when(cardRepository.findByCardIdAndUserEntityUserId(cardID, userId)).thenReturn(Optional.empty());
        CardNotFoundException exception = assertThrows(CardNotFoundException.class, ()->paymentService.deleteCard(cardID,userId));
        assertEquals(Constants.CARD_NOT_FOUND,exception.getMessage());
    }
    @Test
    void reconcilePendingPayments_When_Failed_To_Fetch_RazorPay_Payment() throws Exception{
        Payment razorpayPayment = mock(Payment.class);
        when(razorpayPayment.get("status")).thenReturn("captured");
        when(paymentRepository.findByPaymentStatusAndPaymentMethodNot(any(),any())).thenReturn(List.of(getPaymentEntityA()));
        when(paymentClient.fetch(getPaymentEntityA().getRazorPayPaymentId())).thenReturn(razorpayPayment);
        SuccessResponse response = paymentService.reconcilePendingPayments();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(),response.getStatusCode());
    }

    @Test
    void reconcilePendingPayments_When_PaymentEntity_Is_Empty() throws Exception{
        Payment razorpayPayment = mock(Payment.class);
        when(razorpayPayment.get("status")).thenReturn("captured");
        when(paymentRepository.findByPaymentStatusAndPaymentMethodNot(any(),any())).thenReturn(List.of());
        when(paymentClient.fetch(any())).thenReturn(razorpayPayment);
        SuccessResponse response = paymentService.reconcilePendingPayments();
        assertEquals(Constants.NO_PAYMENTS_AVAILABLE_FOR_RECONCILE,response.getMessage());
    }

    @Test
    void refundProcess_Exception_When_RazorPayPaymentId_Is_Empty() {
        PaymentEntity paymentEntity = getPaymentEntityA();
        paymentEntity.setRazorPayPaymentId("");
        RefundProcessException exception = checkAsserThrows(paymentEntity, getOrderItemEntity());
        assertEquals("Razorpay Payment ID is missing for the refund process.",exception.getMessage());
    }

    @Test
    void refundProcess_Exception_When_RazorPayPaymentId_Is_null() {
        PaymentEntity paymentEntity = getPaymentEntityA();
        paymentEntity.setRazorPayPaymentId(null);
        RefundProcessException exception = checkAsserThrows(paymentEntity, getOrderItemEntity());
        assertEquals("Razorpay Payment ID is missing for the refund process.",exception.getMessage());
    }
    @Test
    void refundProcess_RazorPayException() throws Exception{
        Refund refund = mock(Refund.class);
        when(razorpayClient.payments.refund(anyString())).thenReturn(refund);
        RefundProcessException exception = checkAsserThrows(getPaymentEntityA(), getOrderItemEntity());
        assertEquals(String.format(Constants.RAZORPAY_REFUND_FAILED, getPaymentEntityA().getRazorPayPaymentId()),exception.getMessage());
    }

    private RefundProcessException checkAsserThrows(PaymentEntity paymentEntity, OrderItemEntity orderItemEntity) {
        return assertThrows(RefundProcessException.class, ()-> paymentService.refundProcess(paymentEntity ,orderItemEntity));
    }

    private Optional<PaymentEntity> getPaymentEntity() {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderEntity(new OrderEntity());
        paymentEntity.setPaymentDate(LocalDateTime.now());
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.setPaymentMethod(Constants.RAZORPAY);
        paymentEntity.setUserId("1");
        paymentEntity.setRazorPayOrderId("razorpay123");
        paymentEntity.setPaymentId("pay123");
        paymentEntity.setRazorPayPaymentId("rpayment123");
        paymentEntity.setTotalAmount(100.0);
        paymentEntity.setOrderSubTotal(100.0);
        paymentEntity.setRefundEntities(getListRefundEntities(paymentEntity));
        return Optional.of(paymentEntity);
    }

    private PaymentEntity getPaymentEntityA(){
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentId("Payment1");
        paymentEntity.setOrderEntity(new OrderEntity());
        paymentEntity.setPaymentDate(LocalDateTime.now());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentMethod(Constants.RAZORPAY);
        paymentEntity.setUserId("1");
        paymentEntity.setRazorPayOrderId("razorpay123");
        paymentEntity.setPaymentId("pay123");
        paymentEntity.setRazorPayPaymentId("rpayment123");
        paymentEntity.setTotalAmount(150.0);
        return paymentEntity;
    }

    private PaymentData getPaymentData() {
        PaymentData paymentData = new PaymentData();
        paymentData.setOrderId("order1");
        paymentData.setRazorPayPaymentId("payment1");
        paymentData.setRazorPayOrderId("order1");
        paymentData.setPaymentMethod("card");
        paymentData.setRazorPaySignature("bd0235074695e0aceec56e4848671c5db28faf0aee37391f3d080d4274c21671");
        return paymentData;
    }
    private CardEntity getCardEntity() {
        CardEntity cardEntity = new CardEntity();
        cardEntity.setCardId(generator.generateId(Constants.CARD_ID));
        cardEntity.setUserEntity(getUserEntity());
        cardEntity.setCardType("Rupay");
        cardEntity.setCardLastFourDigits("1234");
        cardEntity.setCardHolderName("jay");
        cardEntity.setRazorPayToken("2024");
        cardEntity.setExpirationDate("12/25");
        return cardEntity;
    }
    private CardRequest getCardRequest() {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setUserId("user1");
        cardRequest.setCardHolderName("John Doe");
        cardRequest.setCardType("Rupay");
        cardRequest.setLastFourDigits("1234");
        cardRequest.setToken("2024");
        cardRequest.setExpirationDate("12/23");
        return cardRequest;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId("123");
        orderEntity.setUserEntity(new UserEntity());
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setOrderItemEntities(getListOfOrderItemEntities(orderEntity));
        orderEntity.setPaymentEntity(new PaymentEntity());
        orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderEntity.setPaymentMethod("Card");
        orderEntity.setAddressEntity(new AddressEntity());
        orderEntity.setTotalAmount(150.0);
        return orderEntity;
    }

    private List<OrderItemEntity> getListOfOrderItemEntities(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(orderEntity);
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(150.0);
        orderItemEntity.setProductOfferPercentage(20.0);
        orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderItemEntity.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
    }

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("123");
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
        userEntity.setAddressEntityList(new ArrayList<>());
        userEntity.setCardEntities(new ArrayList<>());
        return userEntity;
    }

    private PaymentRequest getPaymentRequest() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setUserId("123");
        paymentRequest.setOrderId("456");
        paymentRequest.setPaymentMethod("Card");
        paymentRequest.setTotalAmount(100.0);
        return paymentRequest;
    }

    private List<RefundEntity> getListRefundEntities(PaymentEntity paymentEntity) {
        List<RefundEntity> refundEntities = new ArrayList<>();
        RefundEntity refundEntity = new RefundEntity();
        refundEntity.setRefundId("r123");
        refundEntity.setRefundDate(LocalDateTime.now());
        refundEntity.setRefundStatus(RefundStatus.NOT_REQUESTED);
        refundEntity.setRefundAmount(10.0);
        refundEntity.setRefundIdOrPayoutId("pout_35gfev2j21njk");
        refundEntity.setRefundType(RefundType.PRODUCT);
        refundEntity.setPaymentEntity(paymentEntity);
        refundEntity.setOrderItemEntity(new OrderItemEntity());
        refundEntities.add(refundEntity);
        return refundEntities;
    }

    private OrderItemEntity getOrderItemEntity(){
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(getOrderEntity());
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(150.0);
        orderItemEntity.setProductOfferPercentage(20.0);
        orderItemEntity.setOrderStatus(OrderStatus.DELIVERED);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItemEntity.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        return orderItemEntity;
    }

}