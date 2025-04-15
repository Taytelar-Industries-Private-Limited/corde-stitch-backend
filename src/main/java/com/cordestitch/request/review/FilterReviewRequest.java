package com.cordestitch.request.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterReviewRequest {
    String productId;
    String sortBy;
    String sortOrder;
}
