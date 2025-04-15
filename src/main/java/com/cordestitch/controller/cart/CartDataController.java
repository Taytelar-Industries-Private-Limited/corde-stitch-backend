package com.cordestitch.controller.cart;

import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartItemResponse;
import com.cordestitch.service.service.cart.CartDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class CartDataController {

    private final CartDataService cartDataService;

    @GetMapping("/generate")
    public ResponseEntity<SuccessResponse> generateSession(HttpServletRequest request,HttpServletResponse response) {
        SuccessResponse successResponse = cartDataService.generateSession(request,response);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @PostMapping("/cart/add")
    public ResponseEntity<SuccessResponse> addProductToCart(@RequestBody CartItemRequest cartItemRequest, HttpServletRequest request){
        SuccessResponse successResponse = cartDataService.addProductToCart(cartItemRequest, request);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @GetMapping("/cart/fetch")
    public ResponseEntity<List<CartItemResponse>> fetchCartData(HttpServletRequest request) {
        List<CartItemResponse> cartDataResponse = cartDataService.fetchCartData(request);
        return ResponseEntity.status(HttpStatus.OK).body(cartDataResponse);
    }

    @PutMapping("/cart/update")
    public ResponseEntity<SuccessResponse> updateCartItem(@RequestBody CartItemRequest cartItemRequest, HttpServletRequest request) {
        SuccessResponse successResponse = cartDataService.updateCartItem(cartItemRequest, request);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @DeleteMapping("/cart/remove")
    public ResponseEntity<SuccessResponse> removeProductFromCart(@RequestBody CartItemRequest cartItemRequest,  HttpServletRequest request) {
        SuccessResponse successResponse = cartDataService.removeProductFromCart(cartItemRequest, request);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @DeleteMapping("/cart/delete-items")
    public ResponseEntity<SuccessResponse> deleteCartItems(HttpServletRequest request, HttpServletResponse response) {
        SuccessResponse successResponse = cartDataService.deleteCartItems(request, response);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }
}