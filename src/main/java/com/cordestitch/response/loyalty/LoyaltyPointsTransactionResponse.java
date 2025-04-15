package com.cordestitch.response.loyalty;

import com.cordestitch.enums.LoyaltyTransactionStatus;
import com.cordestitch.enums.LoyaltyTransactionType;
import com.cordestitch.response.order.OrderItemResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoyaltyPointsTransactionResponse {

    private String loyaltyTransactionId;

    private Double pointsChange;

    private LoyaltyTransactionType transactionType;

    private LoyaltyTransactionStatus loyaltyTransactionStatus;

    private String description;

    private String orderItemId;

    private LocalDateTime transactionDate;

    private OrderItemResponse orderItemResponse;
}
