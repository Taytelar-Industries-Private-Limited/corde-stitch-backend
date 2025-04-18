package com.cordestitch.response.product;

import com.cordestitch.response.review.ProductReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDataResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private String productStatus;
    private String productDescription;
    private String productMaterialType;
    private String productPattern;
    private String productStretchType;
    private Double productOfferPercentage;
    private List<StockQuantityResponse> stockQuantityResponseList = new ArrayList<>();
    private List<ProductImageResponse> productImageResponses = new ArrayList<>();
    private String video;
    private ProductReviewResponse productReviewResponse;
}
