package com.cordestitch.response.cart;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CartItemResponse {

    private String cartItemId;
    private String productId;
    private String productName;
    private Integer productSize;
    private String productColor;
    private String productColorCode;
    private Integer quantity;
    private Double price;
    private Double productOfferPercentage;
    private String productImagesUrl;
    private Integer productStocksAvailable;
    private Map<String, String> productAvailableColors = new HashMap<>();
    private List<Integer> productAvailableSizes = new ArrayList<>();
}
