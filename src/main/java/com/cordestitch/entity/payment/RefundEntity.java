package com.cordestitch.entity.payment;

import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.enums.RefundStatus;
import com.cordestitch.enums.RefundType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_data")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RefundEntity {

    @Id
    @Column(name = "refund_id", nullable = false, unique = true)
    private String refundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", referencedColumnName = "payment_id")
    private PaymentEntity paymentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", referencedColumnName = "order_item_id")
    private OrderItemEntity orderItemEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus = RefundStatus.NOT_REQUESTED;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "refund_id_or_payout_id")
    private String refundIdOrPayoutId;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type")
    private RefundType refundType;
}
