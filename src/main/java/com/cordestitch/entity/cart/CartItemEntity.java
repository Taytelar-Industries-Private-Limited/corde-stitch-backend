package com.cordestitch.entity.cart;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CartItemEntity {

    @Field("cart_item_id")
    private String cartItemId;

    @Field("product_id")
    private String productId;

    @Field("product_name")
    private String productName;

    @Field("product_size")
    private Integer productSize;

    @Field("product_color")
    private String productColor;

    @Field("product_color_code")
    private String productColorCode;

    @Field("quantity")
    private Integer quantity;

    @Field("price")
    private Double price;

    @Field("product_offer_percentage")
    private Double productOfferPercentage;

    @Field("product_image")
    private String productImageUrl;
}