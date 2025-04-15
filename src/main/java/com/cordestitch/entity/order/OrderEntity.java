package com.cordestitch.entity.order;

import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders_data")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "redeemed_loyaltyPoints")
    private Double redeemedLoyaltyPoints;

    @Column(name = "order_sub_total")
    private Double orderSubTotal;

    @OneToMany(mappedBy = "orderEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OrderItemEntity> orderItemEntities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity userEntity;

    @OneToOne(mappedBy = "orderEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private PaymentEntity paymentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id", nullable = false)
    @ToString.Exclude
    private AddressEntity addressEntity;

}
