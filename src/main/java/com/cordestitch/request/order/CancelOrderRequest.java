package com.cordestitch.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @NotBlank(message = "orderId cannot be blank")
    private String orderId;

    @NotEmpty(message = "orderItemId cannot be empty")
    private String orderItemId;

    @NotBlank(message = "userId cannot be blank")
    private String userId;

    @NotBlank(message = "reason cannot be blank")
    private String reason;

    private Boolean isRefund;
}