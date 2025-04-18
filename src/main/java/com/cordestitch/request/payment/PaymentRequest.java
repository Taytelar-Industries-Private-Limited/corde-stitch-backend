package com.cordestitch.request.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotBlank(message = "order ID cannot be blank")
    private String orderId;

    @NotNull(message = "Total amount cannot be blank")
    @Positive(message = "Total amount must be a positive value")
    private Double totalAmount;

    @NotBlank(message = "payment method cannot be blank")
    @Pattern(regexp = "COD|RAZORPAY", message = "Payment method can be COD or RAZORPAY")
    private String paymentMethod;

}
