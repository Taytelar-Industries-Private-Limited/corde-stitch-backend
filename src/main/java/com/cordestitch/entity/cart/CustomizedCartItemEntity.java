package com.cordestitch.entity.cart;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomizedCartItemEntity {

    @Field("customized_cart_item_id")
    private String customizedCartItemId;

    @Field("quantity")
    private Integer quantity;

    @Field("price")
    private Double price;

    @Field("color")
    private String color;

    @Field("colorCode")
    private String colorCode;

    @Field("product_offer_percentage")
    private Double productOfferPercentage;

    @Field("product_image")
    private String productImageUrl;

    @Field("customized_product_details")
    private Map<String,Object> customizedProductDetails;
}