package com.cordestitch.repository.order;

import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {

    List<OrderItemEntity> findOrderItemsByReturnStatus(ReturnStatus returnStatus);

    OrderItemEntity findByOrderItemId(String orderItemId);
}
