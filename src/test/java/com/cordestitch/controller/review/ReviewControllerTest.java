package com.cordestitch.controller.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.review.FilterReviewRequest;
import com.cordestitch.request.review.ReviewDataRequest;
import com.cordestitch.request.review.ReviewRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.response.review.ReviewResponse;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @MockBean
    private ReviewService reviewService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    private HttpServletRequest httpServletRequest;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void validateUserPurchase() throws Exception {
        String productId = "PID123";
        when(reviewService.validateUserPurchase(anyString(), anyString())).thenReturn(new SuccessResponse(Constants.USER_PURCHASED_MSG, HttpStatus.OK.value()));
        mockMvc.perform(get("/api/reviews/validateUserPurchase")
                       .requestAttr("userId", DECRYPTED_USER_ID)
                       .param("productId", productId))
                .andExpect(status().isOk());
    }

    @Test
    void createReview() throws Exception{
        ReviewDataRequest request = getReviewDataRequest();
        when(reviewService.createReview(any(ReviewDataRequest.class))).thenReturn(new SuccessResponse(Constants.REVIEW_CREATED_SUCCESSFULLY, HttpStatus.OK.value()));
        mockMvc.perform(post("/api/reviews/createReview")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("comment", request.getComment())
                        .param("rating", String.valueOf(request.getRating()))
                        .param("productId", request.getProductId())
                        .param("orderId", request.getOrderId())
                        .requestAttr("userId", DECRYPTED_USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getProductReviews()  throws Exception{
        String productId = "PID123";
        when(reviewService.getProductReviews(productId)).thenReturn(new ProductReviewResponse());
        mockMvc.perform(get("/api/reviews/getProductReviews")
                       .param("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterReviews() throws Exception{
        FilterReviewRequest request = getFilterReviewRequest();
        when(reviewService.getFilteredReviews(request)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/reviews/filterReviews")
                       .content(objectMapper.writeValueAsString(request))
                       .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateReview() throws Exception {
        ReviewRequest request = getReviewRequest();
        when(reviewService.updateReview(request)).thenReturn(new ReviewResponse());
        mockMvc.perform(put("/api/reviews/updateReview")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("comment", request.getComment())
                        .param("rating", String.valueOf(request.getRating()))
                        .param("productId", request.getProductId())
                        .param("orderId", request.getOrderId())
                        .param("reviewId", request.getReviewId())
                        .requestAttr("userId", request.getUserId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReview() throws Exception{
        String reviewId = "RID123";
        String productId = "PID123";
        when(reviewService.deleteReview(anyString(), anyString(), anyString())).thenReturn(new SuccessResponse(Constants.REVIEW_DELETED_SUCCESSFULLY, HttpStatus.OK.value()));
        mockMvc.perform(delete("/api/reviews/deleteReview")
                       .param("reviewId", reviewId)
                       .param("productId", productId)
                        .requestAttr("userId", DECRYPTED_USER_ID)
                       .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private ReviewRequest getReviewRequest() {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewId("RID123");
        reviewRequest.setProductId("PID123");
        reviewRequest.setOrderId("order123");
        reviewRequest.setUserId(ENCRYPTED_USER_ID);
        reviewRequest.setComment("good");
        reviewRequest.setRating(4);
        return reviewRequest;
    }

    private FilterReviewRequest getFilterReviewRequest() {
        FilterReviewRequest request = new FilterReviewRequest();
        request.setProductId("PID123");
        request.setSortBy(Constants.MOST_RECENT);
        request.setSortOrder("asc");
        return request;
    }

    private ReviewDataRequest getReviewDataRequest() {
        ReviewDataRequest reviewDataRequest = new ReviewDataRequest();
        reviewDataRequest.setComment("good");
        reviewDataRequest.setRating("4");
        reviewDataRequest.setProductId("PID2001");
        reviewDataRequest.setOrderId("order123");
        return reviewDataRequest;
    }
}