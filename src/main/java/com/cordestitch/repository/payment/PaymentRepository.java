package com.cordestitch.repository.payment;

import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity,String> {
    PaymentEntity findByRazorPayOrderId(String razorPayOrderId);

    PaymentEntity findByPaymentId(String paymentId);

    Optional<PaymentEntity> findByOrderEntityOrderId(String orderId);

    List<PaymentEntity> findByPaymentStatusAndPaymentMethodNot(PaymentStatus paymentStatus, String cod);

}
