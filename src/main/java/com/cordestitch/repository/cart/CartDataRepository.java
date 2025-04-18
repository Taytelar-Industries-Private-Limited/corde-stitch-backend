package com.cordestitch.repository.cart;

import com.cordestitch.entity.cart.CartDataEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CartDataRepository extends MongoRepository<CartDataEntity, String> {
    List<CartDataEntity> findByTokenExpiryLessThan(Long currentTime);

    CartDataEntity findByDeviceIdAndTokenId(String deviceId, String tokenId);
}