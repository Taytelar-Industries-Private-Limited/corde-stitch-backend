package com.cordestitch.response.customization;

import lombok.Data;

@Data
public class CustomizationCartResponse {

    private String customizedCartItemId;

    private Integer quantity;

    private Double price;

    private String color;

    private String colorCode;

    private Double productOfferPercentage;

    private String productImageUrl;

    private CustomizedAddDataResponse customizedAddDataResponse;
}