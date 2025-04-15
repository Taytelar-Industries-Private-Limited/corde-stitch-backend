package com.cordestitch.entity.cart;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "cart_data_entity")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CartDataEntity {

    @Id
    @Field(name = "session_id")
    private String sessionId;

    @Field(name = "token_id")
    private String tokenId;

    @Field(name = "device_id")
    private String deviceId;

    @Field(name = "token_expiry")
    private Long tokenExpiry;

    @Field(name = "cart_data_item_entities")
    private List<CartItemEntity> cartDataItemEntities;
}