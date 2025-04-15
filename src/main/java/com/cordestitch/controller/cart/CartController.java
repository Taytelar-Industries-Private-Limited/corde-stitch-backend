package com.cordestitch.controller.cart;

import com.cordestitch.request.cart.CartRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartResponse;
import com.cordestitch.service.service.cart.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Adds items to the cart.
     *
     * @param cartRequest The request body containing user ID and a list of cart items to be added.
     * @return A response entity with a success message and HTTP status code 200 (OK) if the operation is successful.
     */
    @PostMapping("/addToCart")
    public ResponseEntity<SuccessResponse> addToCart(@Valid @RequestBody CartRequest cartRequest) {
        SuccessResponse successResponse = cartService.addToCart(cartRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    /**
     * Updates existing cart items.
     *
     * @param cartRequest The request body containing user ID and a list of cart items to be updated.
     * @return A response entity with a success message and HTTP status code 200 (OK) if the operation is successful.
     */
    @PutMapping("/updateCartItem")
    public ResponseEntity<SuccessResponse> updateCartItem(@Valid @RequestBody CartRequest cartRequest) {
        SuccessResponse successResponse = cartService.updateCartItem(cartRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    /**
     * Deletes cart items based on provided information.
     *
     * @param cartRequest The request body containing user ID and a list of cart items to be deleted.
     * @return A response entity with a success message and HTTP status code 200 (OK) if the operation is successful.
     */
    @DeleteMapping("/deleteCartItem")
    public ResponseEntity<SuccessResponse> deleteCartItem(@Valid @RequestBody CartRequest cartRequest) {
        SuccessResponse successResponse = cartService.deleteCartItem(cartRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    /**
     * Retrieves cart items for a specific user.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @return A response entity with the cart items and HTTP status code 200 (OK) if the operation is successful.
     */
    @GetMapping("/getCartItems")
    public ResponseEntity<CartResponse> getCartItems(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        CartResponse cartResponse = cartService.getCartItems(userId);
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }
}