package com.cordestitch.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckReturnedProductRequest {

    @NotNull(message = "order id cannot be null")
    private String orderId;

    @NotNull(message = "order item id cannot be null")
    private String orderItemId;

    @NotNull(message = "quantity cannot be null")
    @Positive(message = "Value must be greater than zero")
    private Integer returnedQuantity;

}
