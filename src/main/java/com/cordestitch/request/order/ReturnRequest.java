package com.cordestitch.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReturnRequest {

    @NotBlank(message = "orderId cannot be blank")
    private String orderId;

    @NotBlank(message = "orderItemId cannot be blank")
    private String orderItemId;

    @NotBlank(message = "userId cannot be blank")
    private String userId;

    @NotBlank(message = "reason cannot be blank")
    private String reason;

    private String userBankId;

    @NotBlank(message = "returnType cannot be blank")
    @Pattern(regexp = "refund|exchange", message = "returnType must be either refund or exchange")
    private String returnType;

    private OrderItemRequest replacementOrderItemRequest;
}
