package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorDataResponse {

    private Map<String, String> colorWithColorCodes = new HashMap<>();
    private Map<Integer, String> imagesWithPriority = new HashMap<>();
    private Integer availableStock;
    private boolean isColorStockAvailable;
}
