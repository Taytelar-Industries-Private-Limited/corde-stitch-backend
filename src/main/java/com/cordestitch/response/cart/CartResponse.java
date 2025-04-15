package com.cordestitch.response.cart;

import com.cordestitch.response.customization.CustomizationCartResponse;
import lombok.Data;

import java.util.List;

@Data
public class CartResponse {

    private String cartId;

    private List<CartItemResponse> cartItemResponses;

    private List<CustomizationCartResponse> customizationCartResponses;
}
