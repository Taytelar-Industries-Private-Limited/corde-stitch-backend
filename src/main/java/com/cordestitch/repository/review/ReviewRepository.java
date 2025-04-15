package com.cordestitch.repository.review;

import com.cordestitch.entity.review.ProductReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<ProductReview,String> {
    ProductReview findByProductId(String  productId);

    @Query(value = "{ '_id': ?1, 'reviews.user_id': ?0 }")
    Optional<ProductReview> findByUserIdAndProductId(String userId, String productId);

    @Query(value = "{ '_id': ?0, 'reviews.user_id': ?1, 'reviews.order_id': ?2 }")
    Optional<ProductReview> findByProductIdAndUserIdAndOrderId(String productId, String userId, String orderId);
}
