package com.cordestitch.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferToAccountRequest {

    private String userId;

    @NotNull(message = "user bank id cannot be null")
    private String userBankId;

    private String orderId;

    private String orderItemId;

    private Double amount;
}
