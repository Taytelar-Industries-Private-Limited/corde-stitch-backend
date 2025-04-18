package com.cordestitch.request.order;

import com.cordestitch.request.user.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;

    @NotBlank(message = "payment method cannot be blank")
    private String paymentMethod;

    private Double loyaltyPointsToRedeem;

    private List<OrderItemRequest> orderItemRequests;

    @NotNull(message = "Shipping address cannot be null")
    @Valid
    private AddressRequest shippingAddress;
}
