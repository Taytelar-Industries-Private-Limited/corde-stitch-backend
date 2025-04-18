package com.cordestitch.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.request.cart.CartRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartItemResponse;
import com.cordestitch.response.cart.CartResponse;
import com.cordestitch.service.service.cart.CartService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CartController.class)
class CartControllerTest {

    @MockBean
    public CartService cartService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void testAddToCart() throws Exception {
        CartRequest cartRequest = getCartRequest();
        SuccessResponse successResponse = getSuccessResponse();
        when(cartService.addToCart(any())).thenReturn(successResponse);
        mockMvc.perform(post("/api/cart/addToCart")
                        .content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCartItem() throws Exception {
        CartRequest cartRequest = getCartRequest();
        SuccessResponse successResponse = getSuccessResponse();
        when(cartService.updateCartItem(any())).thenReturn(successResponse);
        mockMvc.perform(put("/api/cart/updateCartItem")
                        .content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCartItem() throws Exception {
        CartRequest cartRequest = getCartRequest();
        SuccessResponse successResponse = getSuccessResponse();
        when(cartService.deleteCartItem(any())).thenReturn(successResponse);
        mockMvc.perform(delete("/api/cart/deleteCartItem")
                        .content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCartItem() throws Exception {
        CartResponse cartResponse = getCartResponse();
        when(cartService.getCartItems(any())).thenReturn(cartResponse);
        mockMvc.perform(get("/api/cart/getCartItems")
                        .param("userId", ENCRYPTED_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private CartResponse getCartResponse() {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartId("cart1");
        cartResponse.setCartItemResponses(getListCartItemResponse());
        return cartResponse;
    }

    private List<CartItemResponse> getListCartItemResponse() {
        List<CartItemResponse> cartItemResponses = new ArrayList<>();
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setCartItemId("cartItem1");
        cartItemResponse.setPrice(10.00);
        cartItemResponse.setProductColor("white");
        cartItemResponse.setProductId("product1");
        cartItemResponse.setProductName("shirt");
        cartItemResponse.setProductSize(1);
        cartItemResponse.setQuantity(1);
        cartItemResponses.add(cartItemResponse);
        return cartItemResponses;
    }

    private SuccessResponse getSuccessResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage("added to cart successfully");
        successResponse.setStatusCode(200);
        return successResponse;
    }

    private CartRequest getCartRequest() {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setUserId(ENCRYPTED_USER_ID);
        cartRequest.setCartItemRequests(getListCartItemRequest());
        return cartRequest;
    }

    private List<CartItemRequest> getListCartItemRequest() {
        List<CartItemRequest> cartItemRequests = new ArrayList<>();
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartItemId("cartItem1");
        cartItemRequest.setPrice(10.00);
        cartItemRequest.setProductOfferPercentage(5.0);
        cartItemRequest.setProductColor("white");
        cartItemRequest.setProductColorCode("#000000");
        cartItemRequest.setProductId("product1");
        cartItemRequest.setProductName("shirt");
        cartItemRequest.setProductSize(1);
        cartItemRequest.setQuantity(1);
        cartItemRequests.add(cartItemRequest);
        return cartItemRequests;
    }
}