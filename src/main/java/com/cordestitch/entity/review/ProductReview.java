package com.cordestitch.entity.review;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "product_reviews")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductReview {

    @Id
    @Field(name = "product_id")
    private String productId;

    @Field(name = "reviews")
    private List<Review> reviews = new ArrayList<>();
}
