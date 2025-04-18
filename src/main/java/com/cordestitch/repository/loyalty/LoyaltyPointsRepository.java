package com.cordestitch.repository.loyalty;

import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPointsEntity,String> {

    LoyaltyPointsEntity findByUserEntityUserId(String userId);

    @Query("SELECT l FROM LoyaltyPointsEntity l " +
            "JOIN FETCH l.userEntity " +
            "JOIN FETCH l.loyaltyPointsTransactionEntities " +
            "where l.loyaltyId = :loyaltyId")
    LoyaltyPointsEntity findByLoyaltyId(String loyaltyId);
}