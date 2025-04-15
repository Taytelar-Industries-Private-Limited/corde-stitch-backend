package com.cordestitch.response.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private String productId;

    private List<ReviewResponse> reviews;

    private Double reviewAverage;

    private Integer totalNumberOfReviews;
}
