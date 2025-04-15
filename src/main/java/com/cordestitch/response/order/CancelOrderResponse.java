package com.cordestitch.response.order;

import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CancelOrderResponse {

    private String message;
    private String orderId;
    private LocalDateTime orderDate;
    private LocalDateTime cancelledDate;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;
    private String refundMessage;
}