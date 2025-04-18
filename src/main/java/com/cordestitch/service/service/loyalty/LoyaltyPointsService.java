package com.cordestitch.service.service.loyalty;

import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.loyalty.LoyaltyPointsResponse;
import com.cordestitch.response.loyalty.PointsRedemptionResponse;

import java.util.List;

public interface LoyaltyPointsService {

    SuccessResponse processReferral(String referrerCode,String userId);

    SuccessResponse processOrderLoyaltyPoints(String orderItemId, String userId);

    LoyaltyPointsResponse getLoyaltyPointsSummary(String userId);

    SuccessResponse redeemLoyaltyPoints(String userId, OrderEntity orderId, Double loyaltyPointsToRedeem);

    SuccessResponse refundPointsForCancelledOrderItem(String userId, String orderId, String orderItemId);

    PointsRedemptionResponse redeemMoneyFromPoints(String userId, Double totalRedeemablePoints);

    List<SuccessResponse> entityProcessLoyaltyPointsToAccount();
}