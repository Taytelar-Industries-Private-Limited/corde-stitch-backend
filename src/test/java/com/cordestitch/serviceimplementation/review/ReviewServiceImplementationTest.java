package com.cordestitch.serviceimplementation.review;

import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.review.ProductReview;
import com.cordestitch.entity.review.Review;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.exception.review.UnauthorizedActionException;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.repository.review.ReviewRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.review.FilterReviewRequest;
import com.cordestitch.request.review.ReviewDataRequest;
import com.cordestitch.request.review.ReviewRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.response.review.ReviewResponse;
import com.cordestitch.service.serviceimplementation.review.ReviewServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceImplementationTest {
    @InjectMocks
    private ReviewServiceImplementation reviewServiceImplementation;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Generator generator;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private IdEncryptor idEncryptor;

    @Mock
    private S3Client s3Client;

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(s3Client.utilities()).thenReturn(s3Utilities);

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void createReview_Success() throws Exception{
        String bucketName = "test-bucket";
        Field bucketNameField = ReviewServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(reviewServiceImplementation, bucketName);
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);
        MultipartFile[] images = {image1, image2};

        MultipartFile video = mock(MultipartFile.class);
        when(video.getOriginalFilename()).thenReturn("video.mp4");

        InputStream videoInputStream = new ByteArrayInputStream("dummy-video-content".getBytes());
        when(video.getInputStream()).thenReturn(videoInputStream);

        ReviewDataRequest request = getReviewDataRequest();
        request.setImages(List.of(images));
        request.setVideos(List.of(video));
        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/123/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/123/images/image2.jpg";
        String videoUrl = "https://s3.amazonaws.com/test-bucket/123/video/video.mp4";

        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URI(imageUrl1).toURL())
                .thenReturn(new URI(imageUrl2).toURL())
                .thenReturn(new URI(videoUrl).toURL());


        when(reviewRepository.findByProductIdAndUserIdAndOrderId(anyString(),anyString(),anyString())).thenReturn(Optional.empty());
        when(userRepository.findUserByUserId(any())).thenReturn(new UserEntity());
        when(reviewRepository.findByProductId(any())).thenReturn(new ProductReview());
        SuccessResponse response = reviewServiceImplementation.createReview(request);
        assertEquals(Constants.REVIEW_CREATED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void testCreateReview_When_UserAlreadyProvided_ReviewForProduct() {
        ReviewDataRequest request = getReviewDataRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(reviewRepository.findByProductIdAndUserIdAndOrderId(any(), any(), any())).thenReturn(Optional.of(getProductReview()));
        SuccessResponse response = reviewServiceImplementation.createReview(request);
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void createReview_Exception() throws Exception{
        String bucketName = "test-bucket";
        Field bucketNameField = ReviewServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(reviewServiceImplementation, bucketName);
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);
        MultipartFile[] images = {image1, image2};

        MultipartFile video = mock(MultipartFile.class);
        when(video.getOriginalFilename()).thenReturn("video.mp4");

        InputStream videoInputStream = new ByteArrayInputStream("dummy-video-content".getBytes());
        when(video.getInputStream()).thenReturn(videoInputStream);

        ReviewDataRequest request = getReviewDataRequest();
        request.setImages(List.of(images));
        request.setVideos(List.of(video));
        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/123/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/123/images/image2.jpg";
        String videoUrl = "https://s3.amazonaws.com/test-bucket/123/video/video.mp4";

        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URI(imageUrl1).toURL())
                .thenReturn(new URI(imageUrl2).toURL())
                .thenReturn(new URI(videoUrl).toURL());


        when(userRepository.findUserByUserId(any())).thenReturn(new UserEntity());
        when(reviewRepository.findByProductId(any())).thenReturn(new ProductReview());
        when(video.getInputStream()).thenThrow(new IOException());
        S3UploadException exception = assertThrows(S3UploadException.class,
                () -> reviewServiceImplementation.createReview(request));

        assertEquals(Constants.UPLOAD_ERROR, exception.getMessage());
    }

    @Test
    void createReview_Success_When_ProductReview_Is_Null() throws Exception{
        ReviewDataRequest request = getReviewDataRequest();
        String bucketName = "test-bucket";
        Field bucketNameField = ReviewServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(reviewServiceImplementation, bucketName);
        when(userRepository.findUserByUserId(any())).thenReturn(new UserEntity());
        when(reviewRepository.findByProductId(any())).thenReturn(null);
        SuccessResponse response = reviewServiceImplementation.createReview(request);
        assertEquals(Constants.REVIEW_CREATED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void createReview_Success_When_Images_And_Video_Requests_Are_Empty() throws Exception{
        ReviewDataRequest request = getReviewDataRequest();
        request.setVideos(List.of());
        request.setImages(List.of());
        String bucketName = "test-bucket";
        Field bucketNameField = ReviewServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(reviewServiceImplementation, bucketName);
        when(userRepository.findUserByUserId(any())).thenReturn(new UserEntity());
        when(reviewRepository.findByProductId(any())).thenReturn(null);
        SuccessResponse response = reviewServiceImplementation.createReview(request);
        assertEquals(Constants.REVIEW_CREATED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void updateReview_Success(){
        ReviewRequest request = getReviewRequest();
        ProductReview productReview = getProductReview();
        when(reviewRepository.findByProductId(any())).thenReturn(productReview);
        ReviewResponse response = reviewServiceImplementation.updateReview(request);
        assertNotNull(response);
    }

    @Test
    void updateReview_Exception_When_UserId_Not_Match(){
        ReviewRequest request = getReviewRequest();
        request.setUserId("UID100");
        ProductReview productReview = getProductReview();
        when(reviewRepository.findByProductId(any())).thenReturn(productReview);
        UnauthorizedActionException response = assertThrows(UnauthorizedActionException.class, ()-> reviewServiceImplementation.updateReview(request));
        assertEquals(Constants.UNAUTHORIZED_ACTION, response.getMessage());
    }

    @Test
    void updateReview_Exception_When_ReviewId_Not_Match(){
        ReviewRequest request = getReviewRequest();
        request.setReviewId("RID100");
        ProductReview productReview = getProductReview();
        when(reviewRepository.findByProductId(any())).thenReturn(productReview);
        ResourceNotFoundException response = assertThrows(ResourceNotFoundException.class, ()-> reviewServiceImplementation.updateReview(request));
        assertEquals(Constants.REVIEW_NOT_FOUND, response.getMessage());
    }

    @Test
    void updateReview_Exception_Product_Not_Found(){
        ReviewRequest request = getReviewRequest();
        when(reviewRepository.findByProductId(any())).thenReturn(null);
        ResourceNotFoundException response = assertThrows(ResourceNotFoundException.class, ()-> reviewServiceImplementation.updateReview(request));
        assertEquals(Constants.PRODUCT_NOT_FOUND, response.getMessage());
    }

    @Test
    void deleteReview_Success(){
        String reviewId = "RID123";
        String productId = "PID123";
        ProductReview productReview = getProductReview();
        when(reviewRepository.findByProductId(any())).thenReturn(productReview);
        reviewRepository.save(any());
        SuccessResponse response = reviewServiceImplementation.deleteReview(reviewId,productId, ENCRYPTED_USER_ID);
        assertEquals(Constants.REVIEW_DELETED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void deleteReview_Exception_When_UserId_Not_Match(){
        String reviewId = "RID123";
        String productId = "PID123";
        String userId = "UID123";
        ProductReview productReview = getProductReview();
        when(reviewRepository.findByProductId(any())).thenReturn(productReview);
        reviewRepository.save(any());
        UnauthorizedActionException response = assertThrows(UnauthorizedActionException.class, ()->reviewServiceImplementation.deleteReview(reviewId,productId, userId));
        assertEquals(Constants.UNAUTHORIZED_ACTION, response.getMessage());
    }

    @Test
    void deleteReview_Exception_When_ReviewId_Not_Match(){
        String reviewId = "RID003";
        String productId = "PID123";
        String userId = "UID123";
        ProductReview productReview = getProductReview();
        when(reviewRepository.findByProductId(any())).thenReturn(productReview);
        reviewRepository.save(any());
        ResourceNotFoundException response = assertThrows(ResourceNotFoundException.class, ()->reviewServiceImplementation.deleteReview(reviewId,productId, userId));
        assertEquals(Constants.REVIEW_NOT_FOUND, response.getMessage());
    }

    @Test
    void deleteReview_Exception_When_Product_Not_Found(){
        String reviewId = "RID003";
        String productId = "PID123";
        String userId = "UID123";
        when(reviewRepository.findByProductId(any())).thenReturn(null);
        reviewRepository.save(any());
        ResourceNotFoundException response = assertThrows(ResourceNotFoundException.class, ()->reviewServiceImplementation.deleteReview(reviewId,productId, userId));
        assertEquals(Constants.PRODUCT_NOT_FOUND, response.getMessage());
    }

    @Test
    void getProductReviews_Success(){
        String productId = "PID123";
        when(reviewRepository.findByProductId(any())).thenReturn(getProductReview());
        ProductReviewResponse response = reviewServiceImplementation.getProductReviews(productId);
        assertEquals(productId, response.getProductId());
    }

    @Test
    void getProductReviews_Exception(){
        String productId = "PID123";
        when(reviewRepository.findByProductId(any())).thenReturn(null);
        ProductReviewResponse response = reviewServiceImplementation.getProductReviews(productId);
        assertEquals(new ArrayList<>(), response.getReviews());
    }

    @Test
    void validateUserPurchase_Success(){
        String userId = "UID123";
        String productId = "PID123";
        UserEntity user = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(user);
        SuccessResponse response = reviewServiceImplementation.validateUserPurchase(userId,productId);
        assertEquals(Constants.USER_PURCHASED_MSG, response.getMessage());
    }

    @Test
    void validateUserPurchase_Success_When_Not_Purchased(){
        String userId = "UID123";
        String productId = "PID0001";
        UserEntity user = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(user);
        SuccessResponse response = reviewServiceImplementation.validateUserPurchase(userId,productId);
        assertEquals(Constants.USER_NOT_PURCHASED_MSG, response.getMessage());
    }

    @Test
    void validateUserPurchase_Exception_User_Not_Found(){
        String userId = "UID123";
        String productId = "PID0001";
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->reviewServiceImplementation.validateUserPurchase(userId,productId));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getFilteredReviews_Success_For_MostRecent_Review(){
        FilterReviewRequest request = getFilterReviewRequest();
        when(reviewRepository.findByProductId(any())).thenReturn(getProductReview());
        List<Review> reviews = reviewServiceImplementation.getFilteredReviews(request);
        assertNotNull(reviews);
    }

    @Test
    void getFilteredReviews_Success_For_OldestFirst_Review(){
        FilterReviewRequest request = getFilterReviewRequest();
        request.setSortBy(Constants.OLDEST_FIRST);
        when(reviewRepository.findByProductId(any())).thenReturn(getProductReview());
        List<Review> reviews = reviewServiceImplementation.getFilteredReviews(request);
        assertNotNull(reviews);
    }

    @Test
    void getFilteredReviews_Success_For_HighestRated_Review(){
        FilterReviewRequest request = getFilterReviewRequest();
        request.setSortBy(Constants.HIGHEST_RATED);
        when(reviewRepository.findByProductId(any())).thenReturn(getProductReview());
        List<Review> reviews = reviewServiceImplementation.getFilteredReviews(request);
        assertNotNull(reviews);
    }

    @Test
    void getFilteredReviews_Success_For_LowestRated_Review(){
        FilterReviewRequest request = getFilterReviewRequest();
        request.setSortBy(Constants.LOWEST_RATED);
        when(reviewRepository.findByProductId(any())).thenReturn(getProductReview());
        List<Review> reviews = reviewServiceImplementation.getFilteredReviews(request);
        assertNotNull(reviews);
    }

    @Test
    void getFilteredReviews_Success_For_Default_Filter(){
        FilterReviewRequest request = getFilterReviewRequest();
        request.setSortBy("Default");
        request.setSortOrder("desc");
        when(reviewRepository.findByProductId(any())).thenReturn(getProductReview());
        List<Review> reviews = reviewServiceImplementation.getFilteredReviews(request);
        assertNotNull(reviews);
    }

    @Test
    void getFilteredReviews_Exception_Product_Not_Found(){
        FilterReviewRequest request = getFilterReviewRequest();
        request.setSortBy(Constants.HIGHEST_RATED);
        when(reviewRepository.findByProductId(any())).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->reviewServiceImplementation.getFilteredReviews(request));
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    private FilterReviewRequest getFilterReviewRequest() {
        FilterReviewRequest request = new FilterReviewRequest();
        request.setProductId("PID123");
        request.setSortBy(Constants.MOST_RECENT);
        request.setSortOrder("asc");
        return request;
    }

    private UserEntity getUserEntity() {
        UserEntity user = new UserEntity();
        user.setOrderEntities(new ArrayList<>(List.of(getOrderEntity())));
        return user;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity order = new OrderEntity();
        order.setOrderItemEntities(new ArrayList<>(List.of(getOrderItemEntities())));
        return order;
    }

    private OrderItemEntity getOrderItemEntities() {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setProductId("PID123");
        return orderItem;
    }


    private ProductReview getProductReview() {
        ProductReview productReview = new ProductReview();
        productReview.setProductId("PID123");
        productReview.setReviews(new ArrayList<>(List.of(getReview())));
        return productReview;
    }

    private Review getReview() {
        Review review = new Review();
        review.setReviewId("RID123");
        review.setOrderId("order123");
        review.setUserId(ENCRYPTED_USER_ID);
        review.setUserName("Harish");
        review.setComment("good");
        review.setRating(4);
        return review;
    }

    private ReviewRequest getReviewRequest() {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setReviewId("RID123");
        reviewRequest.setProductId("PID123");
        reviewRequest.setUserId(ENCRYPTED_USER_ID);
        reviewRequest.setComment("good");
        reviewRequest.setRating(4);
        return reviewRequest;
    }

    private ReviewDataRequest getReviewDataRequest() {
        ReviewDataRequest reviewDataRequest = new ReviewDataRequest();
        reviewDataRequest.setUserId(ENCRYPTED_USER_ID);
        reviewDataRequest.setComment("good");
        reviewDataRequest.setRating("4");
        reviewDataRequest.setProductId("PID2001");
        return reviewDataRequest;
    }
}