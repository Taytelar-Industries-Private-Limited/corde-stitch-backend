package com.cordestitch.repository.order;

import com.cordestitch.entity.order.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity,String> {
    OrderEntity findByOrderId(String orderId);
    List<OrderEntity> findAllByUserEntityUserId(String userId);

    @Query("SELECT o FROM OrderEntity o WHERE o.orderId= :orderId AND o.userEntity.userId= :userId")
    OrderEntity findByOrderIdAndUserId(String orderId, String userId);

    Optional<List<OrderEntity>> findByUserEntityUserId(String userId);

    @Query("SELECT o FROM OrderEntity o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderEntity> getReport(LocalDateTime startDate, LocalDateTime endDate);

}
