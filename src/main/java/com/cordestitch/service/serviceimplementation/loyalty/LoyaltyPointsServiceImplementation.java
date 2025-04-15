package com.cordestitch.service.serviceimplementation.loyalty;

import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsTransactionEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.LoyaltyTransactionStatus;
import com.cordestitch.enums.LoyaltyTransactionType;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.exception.review.UnauthorizedActionException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.loyalty.LoyaltyPointsRepository;
import com.cordestitch.repository.loyalty.LoyaltyPointsTransactionRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.loyalty.LoyaltyPointsResponse;
import com.cordestitch.response.loyalty.LoyaltyPointsTransactionResponse;
import com.cordestitch.response.loyalty.PointsRedemptionResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.service.serviceimplementation.order.OrderServiceMappingHelper;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoyaltyPointsServiceImplementation implements LoyaltyPointsService {

    private final Generator generator;

    private final UserRepository userRepository;

    private final LoyaltyPointsRepository loyaltyPointsRepository;

    private final LoyaltyPointsTransactionRepository loyaltyPointsTransactionRepository;

    private final OrderItemRepository orderItemRepository;

    private final OrderRepository orderRepository;

    private final OrderServiceMappingHelper orderServiceMappingHelper;

    private final LoyaltyPointTransactionScheduler loyaltyPointTransactionScheduler;

    private static final double REFERRER_POINTS = 50;
    private static final double NEW_USER_POINTS = 50;
    private static final double LOYALTY_PERCENTAGE = 0.10;
    private static final double POINTS_TO_CURRENCY_RATE = 1.0;       // 100 points = 100 rupees
    private static final double MAX_REDEMPTION_PERCENTAGE = 1.0;    // Max 100% of order total can be redeemed
    private static final double MIN_POINTS_TO_REDEMPTION = 1000.0;

    @Override
    public SuccessResponse processReferral(String referrerCode, String userId) {

        UserEntity userEntity = userRepository.findByUserIdAndIsReferredIsFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(Constants.USER_ALREADY_REFERRED));

        if (!userEntity.getOrderEntities().isEmpty()) {
            throw new UnauthorizedActionException(Constants.USER_HAS_PURCHASE_HISTORY);
        }

        UserEntity referrerEntity = checkReferralCode(referrerCode);

        awardLoyaltyPoints(referrerEntity, REFERRER_POINTS, LoyaltyTransactionType.REFERRAL, Constants.LOYALTY_POINTS_REFERRAL, null, null);

        awardLoyaltyPoints(userEntity, NEW_USER_POINTS, LoyaltyTransactionType.REFERRAL, Constants.LOYALTY_POINTS, true, referrerEntity.getReferralCode());

        return new SuccessResponse(Constants.LOYALTY_POINTS, HttpStatus.OK.value());
    }

    @Transactional
    public SuccessResponse processOrderLoyaltyPoints(String orderItemId,String userId) {

        Optional<OrderItemEntity> optionalOrderItemEntity = orderItemRepository.findById(orderItemId);
        if(optionalOrderItemEntity.isEmpty()){
            throw new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND);
        }

        OrderItemEntity orderItemEntity = optionalOrderItemEntity.get();

        double loyaltyPoints = calculatePotentialLoyaltyPoints(orderItemEntity);

        UserEntity userEntity = orderItemEntity.getOrderEntity().getUserEntity();

        LoyaltyPointsEntity loyaltyPointsEntity = loyaltyPointsRepository.findByUserEntityUserId(userEntity.getUserId());
        if(isNull(loyaltyPointsEntity)){
            loyaltyPointsEntity = new LoyaltyPointsEntity();
            loyaltyPointsEntity.setUserEntity(userEntity);
            loyaltyPointsEntity.setTotalLoyaltyPoints(0.0);
            loyaltyPointsEntity.setTotalRedeemedPoints(0.0);
            loyaltyPointsEntity.setTotalExpiredPoints(0.0);
            loyaltyPointsEntity = loyaltyPointsRepository.save(loyaltyPointsEntity);
        }

        processLoyaltyTransaction(loyaltyPoints, LoyaltyTransactionType.PURCHASE, LoyaltyTransactionStatus.PENDING, Constants.LOYALTY_POINTS_CREDITED, loyaltyPointsEntity,orderItemEntity.getOrderItemId());

        return new SuccessResponse(Constants.LOYALTY_POINTS, HttpStatus.OK.value());
    }

    @Override
    public LoyaltyPointsResponse getLoyaltyPointsSummary(String userId) {

        LoyaltyPointsEntity loyaltyPointsEntity = loyaltyPointsRepository.findByUserEntityUserId(userId);

        if(isNull(loyaltyPointsEntity)){
            throw new ResourceNotFoundException(Constants.LOYALTY_POINTS_NOT_FOUND + userId);
        }

        List<LoyaltyPointsTransactionEntity> transactionEntities = loyaltyPointsTransactionRepository.findTransactionsByLoyaltyId(loyaltyPointsEntity.getLoyaltyId());

        LoyaltyPointsResponse response = new LoyaltyPointsResponse();

        response.setLoyaltyId(loyaltyPointsEntity.getLoyaltyId());
        response.setTotalLoyaltyPoints(loyaltyPointsEntity.getTotalLoyaltyPoints());
        response.setTotalExpiredPoints(loyaltyPointsEntity.getTotalExpiredPoints());
        response.setTotalRedeemedPoints(loyaltyPointsEntity.getTotalRedeemedPoints());
        response.setLastUpdated(loyaltyPointsEntity.getLastUpdated());

        response.setLoyaltyPointsTransactionResponses(
                transactionEntities.stream()
                        .sorted(Comparator.comparing(LoyaltyPointsTransactionEntity::getTransactionDate).reversed())
                        .map(this::mapToTransactionResponse)
                        .toList());

        return response;
    }

    @Override
    public SuccessResponse redeemLoyaltyPoints(String userId, OrderEntity orderEntity, Double loyaltyPointsToRedeem) {

        LoyaltyPointsEntity loyaltyPointsEntity = loyaltyPointsRepository.findByUserEntityUserId(userId);
        if (isNull(loyaltyPointsEntity)) {
            throw new UserNotFoundException(Constants.LOYALTY_POINTS_NOT_FOUND + userId);
        }

        if (loyaltyPointsEntity.getTotalLoyaltyPoints() < loyaltyPointsToRedeem) {
            throw new ResourceNotFoundException(Constants.INSUFFICIENT_LOYALTY_POINTS);
        }

        List<OrderItemEntity> orderItems = orderEntity.getOrderItemEntities();
        log.info("Order items details {}", orderItems);

        double actualPointsToRedeem = calculateRedeemablePoints(loyaltyPointsToRedeem,orderEntity);
        double pointsPerItem = actualPointsToRedeem / orderItems.size();

        loyaltyPointsEntity.setTotalLoyaltyPoints(loyaltyPointsEntity.getTotalLoyaltyPoints() - actualPointsToRedeem);
        loyaltyPointsEntity.setTotalRedeemedPoints(loyaltyPointsEntity.getTotalRedeemedPoints() + actualPointsToRedeem);
        loyaltyPointsEntity.setLastUpdated(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        loyaltyPointsRepository.save(loyaltyPointsEntity);

        for (OrderItemEntity orderItem : orderItems) {
            processLoyaltyTransaction(pointsPerItem, LoyaltyTransactionType.REDEMPTION, LoyaltyTransactionStatus.DEBITED, Constants.LOYALTY_POINTS_DEBITED, loyaltyPointsEntity, orderItem.getOrderItemId());
            log.info("Loyalty points redemption successful for user: {}, order item: {}, points: {}", userId, orderItem.getOrderItemId(), pointsPerItem);
            orderItem.setRedeemedLoyaltyPoints(pointsPerItem);
            orderItemRepository.save(orderItem);
        }
        log.info("Loyalty points redeem successful for user: {}",userId);

        return new SuccessResponse(Constants.LOYALTY_POINTS_DEBITED, HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse refundPointsForCancelledOrderItem(String userId, String orderId, String orderItemId) {

        OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(orderId,userId);
        if (isNull(orderEntity)) {
            throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
        }

        OrderItemEntity orderItem = orderServiceMappingHelper.getOrderItemEntity(orderEntity,orderItemId);

        Double redeemedLoyaltyPoints = orderItem.getRedeemedLoyaltyPoints();
        if (isNull(redeemedLoyaltyPoints) || redeemedLoyaltyPoints < 0) {
            throw new ResourceNotFoundException(Constants.LOYALTY_POINTS_NOT_REDEEMED);
        }

        LoyaltyPointsEntity loyaltyPointsEntity = loyaltyPointsRepository.findByUserEntityUserId(userId);
        if (isNull(loyaltyPointsEntity)) {
            throw new UserNotFoundException(Constants.LOYALTY_POINTS_NOT_FOUND + userId);
        }

        loyaltyPointsEntity.setTotalLoyaltyPoints(loyaltyPointsEntity.getTotalLoyaltyPoints() + redeemedLoyaltyPoints);
        loyaltyPointsEntity.setTotalRedeemedPoints(loyaltyPointsEntity.getTotalRedeemedPoints() - redeemedLoyaltyPoints);
        loyaltyPointsEntity.setLastUpdated(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        loyaltyPointsRepository.save(loyaltyPointsEntity);

        processLoyaltyTransaction(redeemedLoyaltyPoints, LoyaltyTransactionType.CANCELLATION,LoyaltyTransactionStatus.CREDITED, Constants.LOYALTY_POINTS_CREDITED, loyaltyPointsEntity,orderItemId);
        log.info("Loyalty points redemption canceled for order-item: {}, refunded points: {}",orderItemId, redeemedLoyaltyPoints);

        return new SuccessResponse(Constants.LOYALTY_POINTS_CREDITED, HttpStatus.OK.value());
    }

    @Override
    public PointsRedemptionResponse redeemMoneyFromPoints(String userId, Double totalRedeemablePoints) {

        LoyaltyPointsEntity  loyaltyPointsEntity = loyaltyPointsRepository.findByUserEntityUserId(userId);

        if(isNull(loyaltyPointsEntity)){
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        Double availablePoints = loyaltyPointsEntity.getTotalLoyaltyPoints();

        if(availablePoints < totalRedeemablePoints || availablePoints < MIN_POINTS_TO_REDEMPTION){
            throw new ResourceNotFoundException(Constants.INSUFFICIENT_LOYALTY_POINTS);
        }

        processLoyaltyTransaction(totalRedeemablePoints,LoyaltyTransactionType.REDEMPTION,LoyaltyTransactionStatus.PENDING,Constants.LOYALTY_POINTS_REDEEMED,loyaltyPointsEntity,null);
        return new PointsRedemptionResponse(totalRedeemablePoints,Constants.REDEMPTION_INITIATED,HttpStatus.OK.value());
    }

    @Override
    public List<SuccessResponse> entityProcessLoyaltyPointsToAccount() {
        return loyaltyPointTransactionScheduler.processLoyaltyPointsAfterReturnPeriod();
    }

    public void awardLoyaltyPoints(UserEntity userEntity,
                                   double pointsToAward,
                                   LoyaltyTransactionType transactionType,
                                   String description,
                                   Boolean isReferred,
                                   String referredReferralCode) {

        LoyaltyPointsEntity loyaltyPointsEntity = loyaltyPointsRepository.findByUserEntityUserId(userEntity.getUserId());

        loyaltyPointsEntity.setTotalLoyaltyPoints(loyaltyPointsEntity.getTotalLoyaltyPoints() + pointsToAward);
        loyaltyPointsEntity.setLastUpdated(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        if (!isNull(isReferred) && !isNull(referredReferralCode)) {
            loyaltyPointsEntity.setIsReferred(isReferred);
            loyaltyPointsEntity.setReferredReferralCode(referredReferralCode);
            userEntity.setReferred(isReferred);
            userEntity.setReferredReferralCode(referredReferralCode);
            userRepository.save(userEntity);
        }
        loyaltyPointsRepository.save(loyaltyPointsEntity);

        processLoyaltyTransaction(pointsToAward,transactionType,LoyaltyTransactionStatus.CREDITED,description,loyaltyPointsEntity,null);

    }

    private UserEntity checkReferralCode(String referralCode) {
        return userRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new UserNotFoundException(Constants.INVALID_REFERRAL_CODE));
    }


    private void processLoyaltyTransaction(double pointsToChange, LoyaltyTransactionType transactionType, LoyaltyTransactionStatus loyaltyTransactionStatus, String description, LoyaltyPointsEntity loyaltyPointsEntity,String orderItemId) {

        LoyaltyPointsTransactionEntity transaction = new LoyaltyPointsTransactionEntity();
        transaction.setLoyaltyTransactionId(generator.generateId(Constants.LOYALTY_TRANSACTION_ID));
        transaction.setPointsChange(pointsToChange);
        transaction.setTransactionType(transactionType);
        transaction.setLoyaltyTransactionStatus(loyaltyTransactionStatus);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        transaction.setLoyaltyPointsEntity(loyaltyPointsEntity);
        if(!isNull(orderItemId)){
            transaction.setOrderItemId(orderItemId);
        }
        loyaltyPointsTransactionRepository.save(transaction);
    }

    private double calculateRedeemablePoints(double loyaltyPointsToRedeem, OrderEntity orderEntity) {

        double maxRedeemableAmount = orderEntity.getTotalAmount() * MAX_REDEMPTION_PERCENTAGE;
        log.info("Max redeemable amount: {}", maxRedeemableAmount);

        double redeemableValue = loyaltyPointsToRedeem * POINTS_TO_CURRENCY_RATE;
        log.info("Redeemable value in currency: {}", redeemableValue);

        return Math.min(redeemableValue, maxRedeemableAmount) / POINTS_TO_CURRENCY_RATE;
    }

    private double calculatePotentialLoyaltyPoints(OrderItemEntity orderItem) {
        double loyaltyPoints = orderItem.getTotalAmount() * LOYALTY_PERCENTAGE;
        return (Math.round(loyaltyPoints) > loyaltyPoints) ? Math.ceil(loyaltyPoints) : Math.floor(loyaltyPoints);
    }

    private LoyaltyPointsTransactionResponse mapToTransactionResponse(LoyaltyPointsTransactionEntity pointsTransactionEntity) {

        LoyaltyPointsTransactionResponse response = new LoyaltyPointsTransactionResponse();
        response.setLoyaltyTransactionId(pointsTransactionEntity.getLoyaltyTransactionId());
        response.setPointsChange(pointsTransactionEntity.getPointsChange());
        response.setTransactionType(pointsTransactionEntity.getTransactionType());
        response.setLoyaltyTransactionStatus(pointsTransactionEntity.getLoyaltyTransactionStatus());
        response.setDescription(pointsTransactionEntity.getDescription());
        response.setTransactionDate(pointsTransactionEntity.getTransactionDate());
        if (!isNull(pointsTransactionEntity.getOrderItemId())) {
            mapOrderItemResponse(pointsTransactionEntity, response);
        }
        return response;
    }

    private void mapOrderItemResponse(LoyaltyPointsTransactionEntity pointsTransactionEntity, LoyaltyPointsTransactionResponse response) {

        OrderItemEntity orderItemEntity = orderItemRepository.findById(pointsTransactionEntity.getOrderItemId())
                .orElseThrow(() -> new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));

        response.setOrderItemId(orderItemEntity.getOrderItemId());

        if (isNull(orderItemEntity.getUserCustomizationEntity())) {
            response.setOrderItemResponse(orderServiceMappingHelper.mapToOrderItemResponse(orderItemEntity));
        } else {
            response.setCustomizedCartItemResponse(orderServiceMappingHelper.mapToCustomizedCartItemResponse(orderItemEntity));
        }
    }
}