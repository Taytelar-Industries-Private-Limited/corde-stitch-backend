package com.cordestitch.response.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentData {

    @NotBlank(message = "order ID cannot be blank")
    private String orderId;

    @NotBlank(message = "Razorpay Order ID cannot be blank")
    private String razorPayOrderId;

    @NotBlank(message = "Razorpay Payment ID cannot be blank")
    private String razorPayPaymentId;

    @NotBlank(message = "Razorpay Signature ID cannot be blank")
    private String razorPaySignature;

    private String paymentMethod;
}
