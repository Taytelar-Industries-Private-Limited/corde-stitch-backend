package com.cordestitch.controller.review;

import com.cordestitch.entity.review.Review;
import com.cordestitch.request.review.FilterReviewRequest;
import com.cordestitch.request.review.ReviewDataRequest;
import com.cordestitch.request.review.ReviewRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.response.review.ReviewResponse;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/validateUserPurchase")
    public ResponseEntity<SuccessResponse> validateUserPurchase(HttpServletRequest request, String productId) {
        String userId = (String) request.getAttribute(Constants.USERID);
        SuccessResponse successResponse = reviewService.validateUserPurchase(userId, productId);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @PostMapping("/createReview")
    public ResponseEntity<SuccessResponse> createReview(
            @Valid @ModelAttribute ReviewDataRequest reviewDataRequest,
            @RequestAttribute("userId") String userId) {
        reviewDataRequest.setUserId(userId);
        SuccessResponse response = reviewService.createReview(reviewDataRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getProductReviews")
    public ResponseEntity<ProductReviewResponse> getProductReviews(
            @RequestParam String productId) {
        ProductReviewResponse reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/filterReviews")
    public ResponseEntity<List<Review>> filterReviews(@RequestBody FilterReviewRequest filterReviewRequest) {
        List<Review> filteredReviews = reviewService.getFilteredReviews(filterReviewRequest);
        return ResponseEntity.ok(filteredReviews);
    }


    @PutMapping("/updateReview")
    public ResponseEntity<ReviewResponse> updateReview(
            @Valid @ModelAttribute ReviewRequest reviewRequest,
            @RequestAttribute("userId") String userId) {
        reviewRequest.setUserId(userId);
        ReviewResponse reviewResponse = reviewService.updateReview(reviewRequest);
        return ResponseEntity.status(HttpStatus.OK).body(reviewResponse);
    }

    @DeleteMapping("/deleteReview")
    public ResponseEntity<SuccessResponse> deleteReview(@RequestParam String reviewId, @RequestParam String productId,
                                                        HttpServletRequest request) {
        String userId = (String) request.getAttribute(Constants.USERID);
        SuccessResponse successResponse = reviewService.deleteReview(reviewId, productId, userId);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    @GetMapping("/getReviewById")
    public ResponseEntity<ReviewResponse> getReviewById(@RequestParam String productId, @RequestParam String orderId, HttpServletRequest request){
        String userId = (String) request.getAttribute(Constants.USERID);
        ReviewResponse reviewResponse = reviewService.getReviewById(productId, orderId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(reviewResponse);
    }
}
