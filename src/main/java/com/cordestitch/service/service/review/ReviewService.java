package com.cordestitch.service.service.review;

import com.cordestitch.entity.review.Review;
import com.cordestitch.request.review.FilterReviewRequest;
import com.cordestitch.request.review.ReviewDataRequest;
import com.cordestitch.request.review.ReviewRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.response.review.ReviewResponse;

import java.util.List;

public interface ReviewService {
    SuccessResponse createReview(ReviewDataRequest reviewDataRequest);

    ReviewResponse updateReview(ReviewRequest reviewRequest);

    SuccessResponse deleteReview(String reviewId, String productId, String userId);

    ProductReviewResponse getProductReviews(String  productId);

    SuccessResponse validateUserPurchase(String userId, String productId);

    List<Review> getFilteredReviews(FilterReviewRequest filterReviewRequest);

    ReviewResponse getReviewById(String productId, String orderId, String userId);
}
