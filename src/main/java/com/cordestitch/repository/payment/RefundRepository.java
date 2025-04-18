package com.cordestitch.repository.payment;

import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<RefundEntity, String> {
    RefundEntity findByPaymentEntityAndOrderItemEntity(PaymentEntity payment, OrderItemEntity orderItemEntity);

    List<RefundEntity> findAllByRefundStatus(RefundStatus refundStatus);
}
