package com.cordestitch.request.whatsapp;

import lombok.Data;

@Data
public class OrderCancellationResponse {

    private String orderIemId;

    private String orderCancellationReason;
}