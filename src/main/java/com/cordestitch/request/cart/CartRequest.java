package com.cordestitch.request.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartRequest {

    @NotBlank(message = "user id can not be empty or null")
    private String userId;

    @NotNull(message = "Cart items cannot be null")
    @NotEmpty(message = "Cart must contain at least one item")
    @Valid
    private List<CartItemRequest> cartItemRequests;
}
