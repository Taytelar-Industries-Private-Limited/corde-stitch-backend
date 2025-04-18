package com.cordestitch.response.order;

import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.enums.ReturnStatus;
import com.cordestitch.response.review.ReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {

    private String orderItemId;

    private String productId;

    private String productName;

    private String productDescription;

    private String productImage;

    private Integer quantity;

    private Double unitPrice;

    private String productColor;

    private String productSize;

    private Double totalAmount;

    private Double productOfferPercentage;

    private int returnDaysPolicy;

    private DeliveryStatus deliveryStatus;

    private LocalDateTime deliveryDate;

    private LocalDateTime cancelOrderDate;

    private OrderStatus orderStatus;

    private ReturnStatus returnStatus;

    private LocalDateTime returnOrderDate;

    private boolean isPinCodeInBengaluru;

    private ReviewResponse reviewResponse;

}
