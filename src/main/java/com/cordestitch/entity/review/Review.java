package com.cordestitch.entity.review;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Review {

    @Field(name = "review_id")
    private String reviewId;

    @Field(name = "user_id")
    private String userId;

    @Field(name = "user_name")
    private String userName;

    @Field(name = "rating")
    private Integer rating;

    @Field(name = "comment")
    private String comment;

    @Field(name = "description")
    private String description;

    @Field(name = "order_id")
    private String orderId;

    @Field(name = "images")
    private List<String> imagePaths;

    @Field(name = "videos")
    private List<String> videoPaths;

    @Field(name = "createdAt")
    private LocalDateTime createdAt;
}

