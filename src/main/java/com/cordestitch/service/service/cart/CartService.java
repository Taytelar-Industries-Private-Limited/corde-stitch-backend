package com.cordestitch.service.service.cart;

import com.cordestitch.request.cart.CartRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartResponse;

public interface CartService {
    SuccessResponse addToCart(CartRequest cartRequest);

    SuccessResponse updateCartItem(CartRequest cartRequest);

    SuccessResponse deleteCartItem(CartRequest cartRequest);

    CartResponse getCartItems(String userId);
}
