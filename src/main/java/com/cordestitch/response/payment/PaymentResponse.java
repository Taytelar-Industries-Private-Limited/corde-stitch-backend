package com.cordestitch.response.payment;

import lombok.Data;

@Data
public class PaymentResponse {

    private String status;

    private String message;

    private String razorPayOrderId;

    private String paymentDate;

}
