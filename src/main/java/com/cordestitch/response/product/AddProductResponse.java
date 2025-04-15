package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductResponse {

    private String productId;
    private String message;
    private Integer statusCode;
}