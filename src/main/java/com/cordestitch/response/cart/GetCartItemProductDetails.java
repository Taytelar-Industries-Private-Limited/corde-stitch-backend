package com.cordestitch.response.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCartItemProductDetails {

    private Integer stocksAvailable;
    private List<Integer> availableSizes;
    private Map<String, String> availableColors;
}
