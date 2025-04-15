package com.cordestitch.response.order;

import com.cordestitch.enums.OrderStatus;
import com.cordestitch.response.user.AddressResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse {

    private String orderId;

    private LocalDateTime orderDate;

    private OrderStatus orderStatus;

    private List<OrderItemResponse> orderItemResponse;

    private AddressResponse addressResponse;

    private PaymentResponse paymentResponse;
}
