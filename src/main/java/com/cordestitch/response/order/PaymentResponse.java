package com.cordestitch.response.order;

import com.cordestitch.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private String paymentId;

    private PaymentStatus paymentStatus;

    private LocalDateTime paymentDate;

    private String paymentMethod;

    private Double totalAmount;

    private Double redemptionPoints;

    private Double orderSubTotal;
}
