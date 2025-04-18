package com.cordestitch.repository.cart;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.cordestitch.entity.cart.CartEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<CartEntity, String> {
    CartEntity findByUserId(String userId);

    default long deleteItemsByDetails(MongoTemplate mongoTemplate, String userId, String productId, Integer size, String color, Integer quantity) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("cartItemEntityList").elemMatch(Criteria.where("productId").is(productId)
                        .and("productSize").is(size)
                        .and("productColor").is(color)
                        .and("quantity").is(quantity)));

        Update update = new Update().pull("cartItemEntityList", new BasicDBObject()
                .append("productId", productId)
                .append("productSize",size)
                .append("productColor",color)
                .append("quantity",quantity));

        UpdateResult result = mongoTemplate.updateFirst(query,update,CartEntity.class);

        return result.getModifiedCount();
    }
}
