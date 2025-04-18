package com.cordestitch.entity.payment;

import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_data")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(name = "user_id",nullable = false)
    private String userId;

    @Column(name = "razorpay_order_id")
    private String razorPayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorPayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @OneToMany(mappedBy = "paymentEntity", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<RefundEntity> refundEntities = new ArrayList<>();

    @Column(name = "order_sub_total")
    private Double orderSubTotal;

    @Column(name = "redeemed_loyaltyPoints")
    private Double redeemedLoyaltyPoints;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    @ToString.Exclude
    private OrderEntity orderEntity;
}
