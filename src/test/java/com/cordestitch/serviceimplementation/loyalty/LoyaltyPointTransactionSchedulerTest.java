package com.cordestitch.serviceimplementation.loyalty;

import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsTransactionEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.repository.loyalty.LoyaltyPointsRepository;
import com.cordestitch.repository.loyalty.LoyaltyPointsTransactionRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.service.serviceimplementation.loyalty.LoyaltyPointTransactionScheduler;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoyaltyPointTransactionSchedulerTest {
    @Spy
    @InjectMocks
    private LoyaltyPointTransactionScheduler loyaltyPointTransactionScheduler;

    @Mock
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private LoyaltyPointsTransactionRepository loyaltyPointsTransactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void processLoyaltyPointsAfterReturnPeriod() {
        LoyaltyPointsTransactionEntity transactionEntity = getLoyaltyPointsTransactionEntity();
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItem.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItem.setDeliveryDate(LocalDateTime.now().minusDays(7));
        when(orderItemRepository.findById(transactionEntity.getOrderItemId())).thenReturn(Optional.of(orderItem));
        when(loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(any())).thenReturn(List.of(transactionEntity));
        when(loyaltyPointsRepository.findByLoyaltyId(any())).thenReturn(getLoyaltyPointsEntity());
        loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod();
        verify(loyaltyPointTransactionScheduler, times(1)).processLoyaltyPointsAfterReturnPeriod();
    }

    @Test
    void processLoyaltyPointsAfterReturnPeriod_OrderItemNotFound_Exception() {
        LoyaltyPointsTransactionEntity transactionEntity = getLoyaltyPointsTransactionEntity();
        when(loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(any())).thenReturn(List.of(transactionEntity));
        OrderItemNotFoundException exception = assertThrows(OrderItemNotFoundException.class, ()->loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod());
        assertEquals(Constants.ORDER_ITEM_NOT_FOUND, exception.getMessage());
    }

    @Test
    void processLoyaltyPointsAfterReturnPeriod_IsOrderItemReturned() {
        LoyaltyPointsTransactionEntity transactionEntity = getLoyaltyPointsTransactionEntity();
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItem.setReturnStatus(ReturnStatus.REFUND_COMPLETED);
        orderItem.setDeliveryDate(LocalDateTime.now().minusDays(7));
        when(orderItemRepository.findById(transactionEntity.getOrderItemId())).thenReturn(Optional.of(orderItem));
        when(loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(any())).thenReturn(List.of(transactionEntity));
        when(loyaltyPointsRepository.findByLoyaltyId(any())).thenReturn(getLoyaltyPointsEntity());
        loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod();
        verify(loyaltyPointTransactionScheduler, times(1)).processLoyaltyPointsAfterReturnPeriod();
    }

    @Test
    void processLoyaltyPointsAfterReturnPeriod_IsEligibleForLoyaltyPointsConfirmation_With_Not_Delivered() {
        LoyaltyPointsTransactionEntity transactionEntity = getLoyaltyPointsTransactionEntity();
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setDeliveryStatus(DeliveryStatus.RETURNED);
        orderItem.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItem.setDeliveryDate(LocalDateTime.now().minusDays(7));
        when(orderItemRepository.findById(transactionEntity.getOrderItemId())).thenReturn(Optional.of(orderItem));
        when(loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(any())).thenReturn(List.of(transactionEntity));
        when(loyaltyPointsRepository.findByLoyaltyId(any())).thenReturn(getLoyaltyPointsEntity());
        loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod();
        verify(loyaltyPointTransactionScheduler, times(1)).processLoyaltyPointsAfterReturnPeriod();
    }

    @Test
    void processLoyaltyPointsAfterReturnPeriod_IsEligibleForLoyaltyPointsConfirmation_With_CurrentDate_Is_Before_Delivered() {
        LoyaltyPointsTransactionEntity transactionEntity = getLoyaltyPointsTransactionEntity();
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItem.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItem.setDeliveryDate(LocalDateTime.now().minusDays(1));
        when(orderItemRepository.findById(transactionEntity.getOrderItemId())).thenReturn(Optional.of(orderItem));
        when(loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(any())).thenReturn(List.of(transactionEntity));
        when(loyaltyPointsRepository.findByLoyaltyId(any())).thenReturn(getLoyaltyPointsEntity());
        loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod();
        verify(loyaltyPointTransactionScheduler, times(1)).processLoyaltyPointsAfterReturnPeriod();
    }

    @Test
    void processLoyaltyPointsAfterReturnPeriod_When_LoyaltyPointsEntity_Is_Null() {
        LoyaltyPointsTransactionEntity transactionEntity = getLoyaltyPointsTransactionEntity();
        OrderItemEntity orderItem = getOrderItemEntity();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItem.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItem.setDeliveryDate(LocalDateTime.now().minusDays(7));
        when(orderItemRepository.findById(transactionEntity.getOrderItemId())).thenReturn(Optional.of(orderItem));
        when(loyaltyPointsTransactionRepository.findByLoyaltyTransactionStatus(any())).thenReturn(List.of(transactionEntity));
        when(loyaltyPointsRepository.findByLoyaltyId(any())).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod());
        assertEquals(Constants.LOYALTY_POINTS_NOT_FOUND, exception.getMessage());
    }
    private LoyaltyPointsTransactionEntity getLoyaltyPointsTransactionEntity() {
        LoyaltyPointsTransactionEntity transactionEntity = new LoyaltyPointsTransactionEntity();
        transactionEntity.setOrderItemId("IT123");
        transactionEntity.setLoyaltyTransactionId("123");
        transactionEntity.setTransactionDate(LocalDateTime.now());
        transactionEntity.setLoyaltyPointsEntity(getLoyaltyPointsEntity());
        transactionEntity.setPointsChange(50.0);
        transactionEntity.setTransactionType(LoyaltyTransactionType.REFERRAL);
        transactionEntity.setLoyaltyTransactionStatus(LoyaltyTransactionStatus.CREDITED);
        transactionEntity.setDescription("loyalty points are given for the active customers");
        return transactionEntity;
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
        userEntity.setUserId("UID123");
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

    private OrderItemEntity getOrderItemEntity() {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(getOrderEntity());
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
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
}