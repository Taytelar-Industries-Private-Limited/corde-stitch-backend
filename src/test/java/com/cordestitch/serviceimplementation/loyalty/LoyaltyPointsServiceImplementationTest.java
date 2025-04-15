package com.cordestitch.serviceimplementation.loyalty;

import com.cordestitch.entity.customization.UserCustomizationEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsTransactionEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.exception.review.UnauthorizedActionException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.repository.loyalty.LoyaltyPointsRepository;
import com.cordestitch.repository.loyalty.LoyaltyPointsTransactionRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.loyalty.LoyaltyPointsResponse;
import com.cordestitch.response.order.CustomizedCartItemResponse;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.service.serviceimplementation.loyalty.LoyaltyPointsServiceImplementation;
import com.cordestitch.service.serviceimplementation.order.OrderServiceMappingHelper;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class LoyaltyPointsServiceImplementationTest {
    @InjectMocks
    private LoyaltyPointsServiceImplementation loyaltyPointsServiceImplementation;

    @Mock
    private Generator generator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Mock
    private LoyaltyPointsTransactionRepository loyaltyPointsTransactionRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderServiceMappingHelper orderServiceMappingHelper;

    @Mock
    private IdEncryptor idEncryptor;

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void processReferral_Success(){
        String referrerCode = "ABC123";
        String userId = DECRYPTED_USER_ID;
        when(userRepository.findByUserIdAndIsReferredIsFalse(userId)).thenReturn(Optional.of(getUserEntity()));
        when(userRepository.findByReferralCode(referrerCode)).thenReturn(Optional.of(getUserEntity()));
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse successResponse = loyaltyPointsServiceImplementation.processReferral(referrerCode,userId);
        assertEquals(Constants.LOYALTY_POINTS, successResponse.getMessage());
    }

    @Test
    void processReferral_Success_When_Referred_Referrer_Code_Is_Null(){
        String referrerCode = "ABC123";
        String userId = DECRYPTED_USER_ID;
        LoyaltyPointsEntity loyaltyPoints = getLoyaltyPointsEntity();
        UserEntity userEntity =getUserEntity();
        userEntity.setReferredReferralCode(null);
        userEntity.setReferralCode(null);
        userEntity.setLoyaltyPointsEntity(loyaltyPoints);
        loyaltyPoints.setUserEntity(userEntity);
        when(userRepository.findByUserIdAndIsReferredIsFalse(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByReferralCode(referrerCode)).thenReturn(Optional.of(userEntity));
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(loyaltyPoints);
        SuccessResponse successResponse = loyaltyPointsServiceImplementation.processReferral(referrerCode,userId);
        assertEquals(Constants.LOYALTY_POINTS, successResponse.getMessage());
    }

    @Test
    void processReferral_User_Already_Referrer_Exception(){
        String referrerCode = "ABC123";
        UserEntity userEntity = getUserEntity();
        userEntity.setReferred(false);
        when(userRepository.findByUserIdAndIsReferredIsFalse(any())).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->loyaltyPointsServiceImplementation.processReferral(referrerCode, DECRYPTED_USER_ID));
        assertEquals(Constants.USER_ALREADY_REFERRED, exception.getMessage());
    }

    @Test
    void processReferral_User_Has_Purchase_History_Exception(){
        String referrerCode = "ABC123";
        UserEntity userEntity = getUserEntity();
        userEntity.setOrderEntities(List.of(new OrderEntity()));
        when(userRepository.findByUserIdAndIsReferredIsFalse(any())).thenReturn(Optional.of(userEntity));
        UnauthorizedActionException exception = assertThrows(UnauthorizedActionException.class, ()->loyaltyPointsServiceImplementation.processReferral(referrerCode, DECRYPTED_USER_ID));
        assertEquals(Constants.USER_HAS_PURCHASE_HISTORY, exception.getMessage());
    }

    @Test
    void processReferral_Invalid_ReferralCode_Exception(){
        String referrerCode = "ABC123";
        UserEntity userEntity = getUserEntity();
        when(userRepository.findByUserIdAndIsReferredIsFalse(any())).thenReturn(Optional.of(userEntity));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->loyaltyPointsServiceImplementation.processReferral(referrerCode, DECRYPTED_USER_ID));
        assertEquals(Constants.INVALID_REFERRAL_CODE, exception.getMessage());
    }

    @Test
    void processOrderLoyaltyPoints_Success(){
        String orderItemId = "IT123";
        String userId = DECRYPTED_USER_ID;
        OrderItemEntity orderItemEntity = getOrderItemEntity();
        orderItemEntity.getOrderEntity().setUserEntity(getUserEntity());
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse successResponse = loyaltyPointsServiceImplementation.processOrderLoyaltyPoints(orderItemId,userId);
        assertEquals(Constants.LOYALTY_POINTS, successResponse.getMessage());
    }

    @Test
    void processOrderLoyaltyPoints_Success_Return_Ceil_Value(){
        String orderItemId = "IT123";
        String userId = DECRYPTED_USER_ID;
        OrderItemEntity orderItemEntity = getOrderItemEntity();
        orderItemEntity.setTotalAmount(156.0);

        orderItemEntity.getOrderEntity().setUserEntity(getUserEntity());
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse successResponse = loyaltyPointsServiceImplementation.processOrderLoyaltyPoints(orderItemId,userId);
        assertEquals(Constants.LOYALTY_POINTS, successResponse.getMessage());
    }
    @Test
    void processOrderLoyaltyPoints_Success_With_UserEntity_Null(){
        String orderItemId = "IT123";
        String userId = DECRYPTED_USER_ID;
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(getOrderItemEntity()));
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse successResponse = loyaltyPointsServiceImplementation.processOrderLoyaltyPoints(orderItemId,userId);
        assertEquals(Constants.LOYALTY_POINTS, successResponse.getMessage());
    }
    @Test
    void processOrderLoyaltyPoints_OrderItem_Not_Found_Exception(){
        String orderItemId = "IT123";
        String userId = DECRYPTED_USER_ID;
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        OrderItemNotFoundException exception = assertThrows(OrderItemNotFoundException.class, ()->loyaltyPointsServiceImplementation.processOrderLoyaltyPoints(orderItemId,userId));
        assertEquals(Constants.ORDER_ITEM_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getLoyaltyPointsSummary_Success(){
        String userId = DECRYPTED_USER_ID;
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        when(loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(any())).thenReturn(List.of(new LoyaltyPointsTransactionEntity()));
        LoyaltyPointsResponse loyaltyPointsResponse = loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId);
        assertNotNull(loyaltyPointsResponse);
    }

    @Test
    void getLoyaltyPointsSummary_Success_With_OrderItemId(){
        String userId = DECRYPTED_USER_ID;
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setUserCustomizationEntity(new UserCustomizationEntity());
        LoyaltyPointsTransactionEntity loyaltyPointsTransactionEntity = new LoyaltyPointsTransactionEntity();
        loyaltyPointsTransactionEntity.setOrderItemId("OID123");
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        when(loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(any())).thenReturn(List.of(loyaltyPointsTransactionEntity));
        when(orderItemRepository.findById(any())).thenReturn(Optional.of(orderItem));
        when(orderServiceMappingHelper.mapToCustomizedCartItemResponse(any())).thenReturn(new CustomizedCartItemResponse());
        LoyaltyPointsResponse loyaltyPointsResponse = loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId);
        assertNotNull(loyaltyPointsResponse);
    }

    @Test
    void getLoyaltyPointsSummary_Success_With_OrderItemId_And_UserCustomizationEntity_Null(){
        String userId = DECRYPTED_USER_ID;
        OrderItemEntity orderItem = getOrderItemEntity();
        LoyaltyPointsTransactionEntity loyaltyPointsTransactionEntity = new LoyaltyPointsTransactionEntity();
        loyaltyPointsTransactionEntity.setOrderItemId("OID123");
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        when(loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(any())).thenReturn(List.of(loyaltyPointsTransactionEntity));
        when(orderItemRepository.findById(any())).thenReturn(Optional.of(orderItem));
        when(orderServiceMappingHelper.mapToOrderItemResponse(any())).thenReturn(new OrderItemResponse());
        LoyaltyPointsResponse loyaltyPointsResponse = loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId);
        assertNotNull(loyaltyPointsResponse);
    }

    @Test
    void getLoyaltyPointsSummary_OrderItemNotFound_Exception(){
        String userId = DECRYPTED_USER_ID;
        LoyaltyPointsTransactionEntity loyaltyPointsTransactionEntity = new LoyaltyPointsTransactionEntity();
        loyaltyPointsTransactionEntity.setOrderItemId("OID123");
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        when(loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(any())).thenReturn(List.of(loyaltyPointsTransactionEntity));
        OrderItemNotFoundException exception = assertThrows(OrderItemNotFoundException.class, ()-> loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId));
        assertNotNull(Constants.ORDER_ITEM_NOT_FOUND, exception.getMessage());
    }
    @Test
    void getLoyaltyPointsSummary_Success_When_LoyaltyPointsTransactionEntity_Is_Not_Null(){
        String userId = DECRYPTED_USER_ID;
        LoyaltyPointsTransactionEntity loyaltyPointsTransactionEntity = new LoyaltyPointsTransactionEntity();
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        when(loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(any())).thenReturn(List.of(loyaltyPointsTransactionEntity));
        doReturn(new OrderItemResponse()).when(orderServiceMappingHelper).mapToOrderItemResponse(getOrderItemEntity());
        when(productRepository.findByProductId(getOrderItemEntity().getProductId())).thenReturn(Optional.of(new Product()));
        LoyaltyPointsResponse loyaltyPointsResponse = loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId);
        assertNotNull(loyaltyPointsResponse);
    }

    @Test
    void getLoyaltyPointsSummary_Success_When_LoyaltyPointsTransactionEntity_And_UserCustomizationEntity_Are_Not_Null(){
        String userId = DECRYPTED_USER_ID;
        OrderItemEntity orderItemEntity = getOrderItemEntity();
        orderItemEntity.setUserCustomizationEntity(new UserCustomizationEntity());
        LoyaltyPointsTransactionEntity loyaltyPointsTransactionEntity = new LoyaltyPointsTransactionEntity();
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(getLoyaltyPointsEntity());
        when(loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(any())).thenReturn(List.of(loyaltyPointsTransactionEntity));
        doReturn(new CustomizedCartItemResponse()).when(orderServiceMappingHelper).mapToCustomizedCartItemResponse(orderItemEntity);
        when(productRepository.findByProductId(getOrderItemEntity().getProductId())).thenReturn(Optional.of(new Product()));
        LoyaltyPointsResponse loyaltyPointsResponse = loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId);
        assertNotNull(loyaltyPointsResponse);
    }

    @Test
    void getLoyaltyPointsSummary_User_Not_Found_Exception(){
        String userId = DECRYPTED_USER_ID;
        when(loyaltyPointsRepository.findByUserEntityUserId(userId)).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->loyaltyPointsServiceImplementation.getLoyaltyPointsSummary(userId));
        assertEquals(Constants.LOYALTY_POINTS_NOT_FOUND + userId, exception.getMessage());
    }

    @Test
    void redeemLoyaltyPoints_Success(){
        String userId = "123";
        OrderEntity orderEntity = getOrderEntity();
        Double loyaltyPointsToRedeem = 5.0;
        when(loyaltyPointsRepository.findByUserEntityUserId(any())).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse response = loyaltyPointsServiceImplementation.redeemLoyaltyPoints(userId,orderEntity,loyaltyPointsToRedeem);
        assertEquals(Constants.LOYALTY_POINTS_DEBITED, response.getMessage());

    }

    @Test
    void redeemLoyaltyPoints_Success_With_OrderItemId(){
        String userId = "123";
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setOrderItemId("Item1");
        Double loyaltyPointsToRedeem = 5.0;
        when(loyaltyPointsRepository.findByUserEntityUserId(any())).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse response = loyaltyPointsServiceImplementation.redeemLoyaltyPoints(userId,orderEntity,loyaltyPointsToRedeem);
        assertEquals(Constants.LOYALTY_POINTS_DEBITED, response.getMessage());
    }

    @Test
    void redeemLoyaltyPoints_InsufficientLoyaltyPoints_Exception(){
        String userId = "123";
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setOrderItemId("Item1");
        Double loyaltyPointsToRedeem = 150.0;
        when(loyaltyPointsRepository.findByUserEntityUserId(any())).thenReturn(getLoyaltyPointsEntity());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->loyaltyPointsServiceImplementation.redeemLoyaltyPoints(userId,orderEntity,loyaltyPointsToRedeem));
        assertEquals(Constants.INSUFFICIENT_LOYALTY_POINTS, exception.getMessage());
    }

    @Test
    void redeemLoyaltyPoints_LoyaltyPointsNotFound_Exception(){
        String userId = "123";
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setOrderItemId("Item1");
        Double loyaltyPointsToRedeem = 150.0;
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->loyaltyPointsServiceImplementation.redeemLoyaltyPoints(userId,orderEntity,loyaltyPointsToRedeem));
        assertEquals(Constants.LOYALTY_POINTS_NOT_FOUND + userId, exception.getMessage());
    }
    @Test
    void cancelOrderItemLoyaltyPoints_Success(){
        String userId = "UID123";
        String orderId = "OID123";
        String orderItemId = "IT123";
        when(orderRepository.findByOrderIdAndUserId(any(),any())).thenReturn(getOrderEntity());
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(getOrderItemEntity());
        when(loyaltyPointsRepository.findByUserEntityUserId(any())).thenReturn(getLoyaltyPointsEntity());
        SuccessResponse response = loyaltyPointsServiceImplementation.refundPointsForCancelledOrderItem(userId,orderId,orderItemId);
        assertEquals(Constants.LOYALTY_POINTS_CREDITED,response.getMessage());
    }
    @Test
    void cancelOrderItemLoyaltyPoints_LoyaltyPointsNotFound_Exception(){
        String userId = "UID123";
        String orderId = "OID123";
        String orderItemId = "IT123";
        when(orderRepository.findByOrderIdAndUserId(any(),any())).thenReturn(getOrderEntity());
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(getOrderItemEntity());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->loyaltyPointsServiceImplementation.refundPointsForCancelledOrderItem(userId,orderId,orderItemId));
        assertEquals(Constants.LOYALTY_POINTS_NOT_FOUND + userId,exception.getMessage());
    }

    @Test
    void cancelOrderItemLoyaltyPoints_LoyaltyPointsNotRedeemed_Exception_When_Null(){
        String userId = "UID123";
        String orderId = "OID123";
        String orderItemId = "IT123";
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setRedeemedLoyaltyPoints(null);
        when(orderRepository.findByOrderIdAndUserId(any(),any())).thenReturn(getOrderEntity());
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderItem);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->loyaltyPointsServiceImplementation.refundPointsForCancelledOrderItem(userId,orderId,orderItemId));
        assertEquals(Constants.LOYALTY_POINTS_NOT_REDEEMED, exception.getMessage());
    }

    @Test
    void cancelOrderItemLoyaltyPoints_LoyaltyPointsNotRedeemed_Exception_When_LessThanZero(){
        String userId = "UID123";
        String orderId = "OID123";
        String orderItemId = "IT123";
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setRedeemedLoyaltyPoints((double) -1);
        when(orderRepository.findByOrderIdAndUserId(any(),any())).thenReturn(getOrderEntity());
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderItem);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->loyaltyPointsServiceImplementation.refundPointsForCancelledOrderItem(userId,orderId,orderItemId));
        assertEquals(Constants.LOYALTY_POINTS_NOT_REDEEMED, exception.getMessage());
    }

    @Test
    void cancelOrderItemLoyaltyPoints_OrderNotFound_Exception(){
        String userId = "UID123";
        String orderId = "OID123";
        String orderItemId = "IT123";
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, ()->loyaltyPointsServiceImplementation.refundPointsForCancelledOrderItem(userId,orderId,orderItemId));
        assertEquals(Constants.ORDER_NOT_FOUND, exception.getMessage());
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
        orderEntity.setPaymentEntity(getPaymentEntity());
        orderEntity.setOrderStatus(OrderStatus.DELIVERED);
        orderEntity.setPaymentMethod(Constants.RAZORPAY);
        orderEntity.setAddressEntity(new AddressEntity());
        orderEntity.setTotalAmount(150.0);
        List<OrderItemEntity> orderItemEntities = List.of(new OrderItemEntity());
        orderEntity.setOrderItemEntities(orderItemEntities);
        return orderEntity;
    }

    private PaymentEntity getPaymentEntity() {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderEntity(new OrderEntity());
        paymentEntity.setPaymentDate(LocalDateTime.now());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentMethod(Constants.RAZORPAY);
        paymentEntity.setUserId("1");
        paymentEntity.setRazorPayOrderId("razorpay123");
        paymentEntity.setPaymentId("pay123");
        paymentEntity.setRazorPayPaymentId("rpayment123");
        paymentEntity.setTotalAmount(150.0);
        paymentEntity.setRefundEntities(getListRefundEntities(paymentEntity));
        return paymentEntity;
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


    private LoyaltyPointsEntity getLoyaltyPointsEntity() {
        LoyaltyPointsEntity loyaltyPoints = new LoyaltyPointsEntity();
        loyaltyPoints.setLoyaltyId("l1");
        loyaltyPoints.setTotalLoyaltyPoints(100.0);
        loyaltyPoints.setTotalExpiredPoints(100.00);
        loyaltyPoints.setTotalRedeemedPoints(50.00);
        loyaltyPoints.setLastUpdated(LocalDateTime.now());
        loyaltyPoints.setUserEntity(getUserEntity());
        loyaltyPoints.setLoyaltyPointsTransactionEntities(new ArrayList<>());
        loyaltyPoints.setIsReferred(true);
        loyaltyPoints.setReferredReferralCode("IJK123");
        return loyaltyPoints;
    }

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(DECRYPTED_USER_ID);
        userEntity.setGender("male");
        userEntity.setReferred(true);
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


}