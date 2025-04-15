package com.cordestitch.service.service.cart;

import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartItemResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface CartDataService {

    SuccessResponse generateSession(HttpServletRequest request, HttpServletResponse response);

    SuccessResponse addProductToCart(CartItemRequest cartItemRequest, HttpServletRequest request);

    List<CartItemResponse> fetchCartData(HttpServletRequest request);

    SuccessResponse updateCartItem(CartItemRequest cartItemRequest, HttpServletRequest request);

    SuccessResponse removeProductFromCart(CartItemRequest cartItemRequest, HttpServletRequest request);

    SuccessResponse deleteCartItems(HttpServletRequest request, HttpServletResponse response);
}