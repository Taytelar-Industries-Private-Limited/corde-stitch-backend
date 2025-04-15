package com.cordestitch.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderExchangeResponse {

    private boolean isAdditionalAmountRequired;

    private double additionalAmount;

    private String orderId;

    private String message;
}
