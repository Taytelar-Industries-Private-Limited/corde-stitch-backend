package com.cordestitch.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.cart.CartDataService;
import com.cordestitch.service.service.token.JwtService;
import com.cordestitch.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(CartDataController.class)
class CartDataControllerTest {

    @MockBean
    public CartDataService cartDataService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
               .webAppContextSetup(webApplicationContext)
               .build();
    }


    @Test
    void testGenerateSession() throws Exception {
        when(cartDataService.generateSession(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(new SuccessResponse(Constants.SUCCESS, HttpStatus.OK.value()));

        mockMvc.perform(get("/api/session/generate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddProductToCart() throws Exception {
        CartItemRequest request = getCartItemRequest();
        when(cartDataService.addProductToCart(any(CartItemRequest.class), any(HttpServletRequest.class)))
                .thenReturn(new SuccessResponse(Constants.ADDED_TO_CART_SUCCESSFULLY, HttpStatus.OK.value()));

        mockMvc.perform(post("/api/session/cart/add")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFetchCartData() throws Exception {
        when(cartDataService.fetchCartData(any(HttpServletRequest.class))).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/session/cart/fetch")
               .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCartItem() throws Exception {
        CartItemRequest cartItemRequest = getCartItemRequest();
        when(cartDataService.updateCartItem(any(CartItemRequest.class), any(HttpServletRequest.class)))
                .thenReturn(new SuccessResponse(Constants.CART_ITEM_UPDATE, HttpStatus.OK.value()));

        mockMvc.perform(put("/api/session/cart/update")
               .content(objectMapper.writeValueAsString(cartItemRequest))
               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    void testRemoveCartItem() throws Exception {
        CartItemRequest request = getCartItemRequest();
        when(cartDataService.removeProductFromCart(any(CartItemRequest.class), any(HttpServletRequest.class)))
                .thenReturn(new SuccessResponse(Constants.CART_ITEM_DELETED, HttpStatus.OK.value()));

        mockMvc.perform(delete("/api/session/cart/remove")
               .content(objectMapper.writeValueAsString(request))
               .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCartItems() throws Exception {
        when(cartDataService.deleteCartItems(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(new SuccessResponse(Constants.CART_ITEM_DELETED, HttpStatus.OK.value()));

        mockMvc.perform(delete("/api/session/cart/delete-items")
               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    private CartItemRequest getCartItemRequest() {
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartItemId("123456");
        cartItemRequest.setProductId("12345");
        cartItemRequest.setProductName("Test Product");
        cartItemRequest.setProductSize(1);
        cartItemRequest.setProductColor("Red");
        cartItemRequest.setProductColorCode("#FF0000");
        cartItemRequest.setQuantity(1);
        cartItemRequest.setPrice(100.0);
        cartItemRequest.setProductOfferPercentage(10.0);
        return cartItemRequest;
    }
}