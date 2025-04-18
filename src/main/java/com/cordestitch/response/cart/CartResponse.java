package com.cordestitch.response.cart;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {

    private String cartId;

    private List<CartItemResponse> cartItemResponses;
}
