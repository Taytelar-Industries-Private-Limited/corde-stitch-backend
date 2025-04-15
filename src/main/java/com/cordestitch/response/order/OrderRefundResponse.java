package com.cordestitch.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRefundResponse {

    private Double refundAmount;

    private String message;
}
