package com.cordestitch.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
public class OrderItemRequest {

    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotBlank(message = "Color cannot be blank")
    private String color;

    @NotNull(message = "Size cannot be blank")
    private Integer size;

    @NotNull(message = "Offer percentage cannot be null")
    @Positive(message = "Offer percentage must be positive")
    private Double offerPercentage;

    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;
}
