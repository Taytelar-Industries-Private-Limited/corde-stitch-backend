package com.cordestitch.response.order;

import com.cordestitch.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOrdersResponse {

    private String orderId;

    private LocalDateTime orderDate;

    private OrderStatus orderStatus;

    private List<OrderItemResponse> orderItemResponse;

    private List<CustomizedCartItemResponse>  customizedCartItemResponses;

    private PaymentResponse paymentResponse;
}
