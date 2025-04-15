package com.cordestitch.request.alteration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlterationRequest {

    @NotBlank(message = "User Id is required")
    private String userId;

    @NotBlank(message = "Order Id is required")
    private String orderId;
}