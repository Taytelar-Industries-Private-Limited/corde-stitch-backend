package com.cordestitch.repository.loyalty;

import com.cordestitch.entity.loyalty.LoyaltyPointsTransactionEntity;
import com.cordestitch.enums.LoyaltyTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointsTransactionRepository extends JpaRepository<LoyaltyPointsTransactionEntity,String> {

    @Query("SELECT t FROM LoyaltyPointsTransactionEntity t " +
            "JOIN FETCH t.loyaltyPointsEntity " +
            "WHERE t.loyaltyTransactionStatus = :status")
    List<LoyaltyPointsTransactionEntity> findByLoyaltyTransactionStatus(@Param("status") LoyaltyTransactionStatus status);

    @Query("SELECT l FROM LoyaltyPointsTransactionEntity l " +
            "WHERE l.loyaltyPointsEntity.loyaltyId = :loyaltyId " +
            "ORDER BY l.transactionDate DESC")
    List<LoyaltyPointsTransactionEntity> findTransactionsByLoyaltyId(@Param("loyaltyId") String loyaltyId);

}
