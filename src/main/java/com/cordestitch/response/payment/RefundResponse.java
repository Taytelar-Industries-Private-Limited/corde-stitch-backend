package com.cordestitch.response.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponse {

    private String refundRazorpayId;

    private String razorPayPaymentId;

    private String refundIdOrPayoutId;

    private Double refundAmount;

    private String refundStatus;

    private LocalDateTime refundDate;
}
